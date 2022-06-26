package com.lucaspetrini.consult.service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lucaspetrini.consult.exception.DatabaseException;
import com.lucaspetrini.consult.service.model.Rating;
import com.lucaspetrini.consult.service.model.UserRating;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Implementation of {@link RatingService} that stores and retrieves
 * {@link Rating ratings} from Amazon DynamoDB.
 */
public class DynamoDbRatingService implements RatingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbRatingService.class);
	private static final String RATINGS = "ratings";
	private static final String RATINGS_SKU = "sku";
	private static final long DEFAULT_TIMEOUT_SECONDS = 100;

	private DynamoDbEnhancedClient dynamoDbEnhancedClient;
	private DynamoDbTable<Rating> ratingsTable;
	private String ratingsTableName;

	/**
	 * Implementation of {@link RatingService} that stores and retrieves
	 * {@link Rating ratings} from Amazon DynamoDB.
	 */
	public DynamoDbRatingService() {
		initDefaults(null);
	}

	/**
	 * Implementation of {@link RatingService} that stores and retrieves
	 * {@link Rating ratings} from Amazon DynamoDB.
	 * 
	 * @param dynamoDbClient underlying {@link DynamoDbClient}.
	 */
	public DynamoDbRatingService(DynamoDbClient dynamoDbClient) {
		initDefaults(dynamoDbClient);
	}

	private void initDefaults(DynamoDbClient dynamoDbClient) {
		if (dynamoDbClient != null) {
			setDynamoDbClient(dynamoDbClient);
		} else {
			setDynamoDbClient(DynamoDbClient.create());
		}
		withRatingsTable(RATINGS);
	}

	private void setDynamoDbClient(DynamoDbClient dynamoDbClient) {
		this.dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
		mapTables(dynamoDbEnhancedClient);
	}

	// TODO an IoC container would be very useful to handle singletons and avoid
	// redundancy
	private void mapTables(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
		ratingsTable = dynamoDbEnhancedClient.table(ratingsTableName, TableSchema.fromBean(Rating.class));
	}

	/**
	 * Set the table name to be used for {@link UserRating user ratings}.
	 * 
	 * @param ratingsTable table name.
	 * @return this {@link DynamoDbUserRatingService} instance.
	 */
	public DynamoDbRatingService withRatingsTable(String ratingsTable) {
		this.ratingsTableName = ratingsTable;
		mapTables(dynamoDbEnhancedClient);
		return this;
	}

	/**
	 * Return a {@link Rating} from the database.
	 * 
	 * @param code    SKU.
	 * @param version rating version.
	 * @return rating.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	protected Rating getItem(String code, Long version) throws InterruptedException, ExecutionException {
		Key key = Key.builder().partitionValue(code).sortValue(version).build();
		return ratingsTable.getItem(key);
	}

	/**
	 * Return the latest {@link Rating} from the database.
	 * 
	 * @param code SKU.
	 * @return latest rating.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	protected Rating getLatestItem(String code) {
		Key key = Key.builder().partitionValue(AttributeValue.fromS(code)).build();
		QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
		QueryEnhancedRequest request = QueryEnhancedRequest.builder().limit(1).queryConditional(queryConditional)
				.scanIndexForward(false).build();
		Rating rating = null;
		try {
			Iterator<Page<Rating>> iterator = ratingsTable.query(request).iterator();
			if (iterator.hasNext()) {
				List<Rating> items = iterator.next().items();
				rating = items.size() > 0 ? items.get(0) : null;
			}
		} catch (Exception e) {
			LOGGER.error("Caught exception: " + e.getMessage());
			throw e;
		}
		return rating;
	}

	protected Rating cloneRating(Rating rating) {
		if(rating == null)
			return null;
		Rating newRating = new Rating();
		newRating.setSku(rating.getSku());
		newRating.setVersion(rating.getVersion());
		newRating.setDate(rating.getDate());
		newRating.setAggregated(rating.getAggregated());
		newRating.setQuantity(rating.getQuantity());
		return newRating;
	}

	@Override
	public Rating getByCode(String code) {
		try {
			return getLatestItem(code);
		} catch (Exception e) {
			LOGGER.error("Caught exception: " + e.getMessage());
			e.printStackTrace();
			throw new DatabaseException(e);
		}
	}

}
