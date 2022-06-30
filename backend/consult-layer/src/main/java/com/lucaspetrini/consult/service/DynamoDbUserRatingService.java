package com.lucaspetrini.consult.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lucaspetrini.consult.exception.DatabaseException;
import com.lucaspetrini.consult.service.model.Rating;
import com.lucaspetrini.consult.service.model.UserRating;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.MappedTableResource;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest.Builder;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Implementation of {@link UserRatingService} that stores and retrieves {@link UserRating user ratings}
 * from Amazon DynamoDB.
 */
public class DynamoDbUserRatingService implements UserRatingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbUserRatingService.class);
	private static final String VERSION_PREFIX = "v0-";
	private static final String USER_RATINGS = "user_ratings";
	private static final String RATINGS = "ratings";

	private DynamoDbClient dynamoDbClient;
	private DynamoDbEnhancedClient dynamoDbEnhancedClient;
	private DynamoDbTable<UserRating> userRatingsTable;
	private DynamoDbTable<Rating> ratingsTable;
	private DynamoDbRatingService ratingService;
	private String userRatingsTableName;
	private String ratingsTableName;

	/**
	 * Implementation of {@link UserRatingService} that stores and retrieves {@link UserRating user ratings}
	 * from Amazon DynamoDB.
	 */
	public DynamoDbUserRatingService() {
		initDefaults(null);
	}

	/**
	 * Implementation of {@link UserRatingService} that stores and retrieves {@link UserRating user ratings}
	 * from Amazon DynamoDB.
	 * 
	 * @param dynamoDbClient underlying {@link DynamoDbClient}.
	 */
	public DynamoDbUserRatingService(DynamoDbClient dynamoDbClient) {
		initDefaults(dynamoDbClient);
	}

	private void initDefaults(DynamoDbClient dynamoDbClient) {
		if(dynamoDbClient != null) {
			setDynamoDbClient(dynamoDbClient);
		}
		else {
			setDynamoDbClient(DynamoDbClient.create());
		}
		withUserRatingsTable(USER_RATINGS);
		withRatingsTable(RATINGS);
	}

	/**
	 * Set the table name to be used for {@link UserRating user ratings}.
	 * 
	 * @param userRatingsTable table name.
	 * @return this {@link DynamoDbUserRatingService} instance.
	 */
	public DynamoDbUserRatingService withUserRatingsTable(String userRatingsTable) {
		this.userRatingsTableName = userRatingsTable;
		mapTables(dynamoDbEnhancedClient);
		return this;
	}

	/**
	 * Set the table name to be used for {@link Rating ratings}.
	 * 
	 * @param ratingsTable table name.
	 * @return this {@link DynamoDbUserRatingService} instance.
	 */
	public DynamoDbUserRatingService withRatingsTable(String ratingsTable) {
		this.ratingsTableName = ratingsTable;
		mapTables(dynamoDbEnhancedClient);
		return this;
	}

	private void setDynamoDbClient(DynamoDbClient dynamoDbClient) {
		this.dynamoDbClient = dynamoDbClient;
		this.dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
		mapTables(dynamoDbEnhancedClient);
	}

	// TODO an IoC container would be very useful to handle singletons and avoid redundancy
	private void mapTables(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
		userRatingsTable = dynamoDbEnhancedClient.table(userRatingsTableName, TableSchema.fromBean(UserRating.class));
		ratingsTable = dynamoDbEnhancedClient.table(ratingsTableName, TableSchema.fromBean(Rating.class));
		ratingService = new DynamoDbRatingService(dynamoDbClient).withRatingsTable(ratingsTableName);
	}

	@Override
	public UserRating getByUserIdAndCode(String userId, String code) {
		try {
			UserRating item = getItem(code, VERSION_PREFIX + userId);
			if(item != null) {
				int versionSeparatorPosition = item.getUser().lastIndexOf('-');
				if(versionSeparatorPosition >= 0) {
					item.setUser(item.getUser().substring(versionSeparatorPosition + 1));
				}
			}
			return item;
		} catch (Exception e) {
			LOGGER.error("Caught exception: " + e.getMessage());
			e.printStackTrace();
			throw new DatabaseException(e);
		}
	}

	// TODO refactor
	@Override
	public UserRating put(UserRating userRating) {
		UserRating newUserRating = null;
		Rating rating = null;
		try {
			UserRating currentUserRating = getItem(userRating.getSku(), VERSION_PREFIX + userRating.getUser());
			Rating currentRating = ratingService.getLatestItem(userRating.getSku());
			Builder transactWriteRequestBuilder = TransactWriteItemsEnhancedRequest.builder();
			if(currentUserRating == null) {
				newUserRating = cloneUserRating(userRating);
				newUserRating.setUser(VERSION_PREFIX + newUserRating.getUser());
				newUserRating.setVersion(1L);
				addToTransaction(transactWriteRequestBuilder, userRatingsTable, newUserRating);
			}
			else {
				// if a rating already exists for the sku and user, create a new item that should be a copy of the current one
				// and update "v0-" with the new information
				// get user name without version prefix (vxxx-)
				String user = currentUserRating.getUser();
				Long version = currentUserRating.getVersion();
				user = user.substring(user.indexOf('-'));
				// set version prefix
				currentUserRating.setUser("v" + version + user);
				addToTransaction(transactWriteRequestBuilder, userRatingsTable, currentUserRating);
				newUserRating = cloneUserRating(userRating);
				newUserRating.setUser(VERSION_PREFIX + userRating.getUser());
				newUserRating.setVersion(currentUserRating.getVersion() + 1);
				addToTransaction(transactWriteRequestBuilder, userRatingsTable, newUserRating, true);
			}

			rating = new Rating();
			rating.setSku(userRating.getSku());
			if(currentRating == null) {
				rating.setAggregated(userRating.getRating());
				rating.setQuantity(1L);
				rating.setDate(userRating.getDate());
				rating.setVersion(1L);
				addToTransaction(transactWriteRequestBuilder, ratingsTable, rating);
			}
			else {
				rating.setDate(userRating.getDate());
				rating.setVersion(currentRating.getVersion() + 1);
				// only increment quantity and aggregation if it's a new user
				if(currentUserRating == null) {
					rating.setQuantity(currentRating.getQuantity() + 1);
					rating.setAggregated(currentRating.getAggregated() + userRating.getRating());
				}
				else {
					rating.setQuantity(currentRating.getQuantity());
					rating.setAggregated(currentRating.getAggregated() - currentUserRating.getRating() + userRating.getRating());
				}
				addToTransaction(transactWriteRequestBuilder, ratingsTable, rating);
			}
			dynamoDbEnhancedClient.transactWriteItems(transactWriteRequestBuilder.build());
		} catch (Exception e) {
			LOGGER.error("Caught exception: " + e.getMessage());
			throw new DatabaseException(e);
		}
		newUserRating.setUser(userRating.getUser());
		return newUserRating;
	}

	@SuppressWarnings("unchecked")
	private <T> void addToTransaction(Builder transactWriteRequestBuilder, MappedTableResource<T> table, T item, boolean override) {
		software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest.Builder<T> builder = TransactPutItemEnhancedRequest.builder((Class<T>)item.getClass())
				.item(item);
		if(!override) {
			builder.conditionExpression(Expression.builder().expression("attribute_not_exists(sku)").build());
		}
		transactWriteRequestBuilder.addPutItem(table, builder.build());
	}

	private <T> void addToTransaction(Builder transactWriteRequestBuilder, MappedTableResource<T> table, T item) {
		addToTransaction(transactWriteRequestBuilder, table, item, false);
	}

	/**
	 * Return a new {@link UserRating} instance identical to the one passed as parameter.
	 * 
	 * @param userRating instance to clone.
	 * @return cloned user rating.
	 */
	protected UserRating cloneUserRating(UserRating userRating) {
		if(userRating == null)
			return null;
		UserRating newUserRating = new UserRating();
		newUserRating.setSku(userRating.getSku());
		newUserRating.setUser(userRating.getUser());
		newUserRating.setVersion(userRating.getVersion());
		newUserRating.setDate(userRating.getDate());
		newUserRating.setRating(userRating.getRating());
		newUserRating.setReview(userRating.getReview());
		return newUserRating;
	}

	/**
	 * Get a single item from the database.
	 * 
	 * @param code user rating code.
	 * @param user user rating user id.
	 * @return item, or null if it doesn't exist.
	 */
	protected UserRating getItem(String code, String user) {
		Key key = Key.builder()
                .partitionValue(code)
                .sortValue(user)
                .build();
		return userRatingsTable.getItem(key);
	}
}
