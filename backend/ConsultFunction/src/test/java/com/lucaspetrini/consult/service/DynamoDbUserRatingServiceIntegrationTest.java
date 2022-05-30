package com.lucaspetrini.consult.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucaspetrini.consult.exception.DatabaseException;
import com.lucaspetrini.consult.service.model.Rating;
import com.lucaspetrini.consult.service.model.UserRating;
import com.lucaspetrini.consult.utils.DynamoDBExtension;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

/**
 * Tests for {@link DynamoDbUserRatingService}. When a new user rating is
 * created for a code that does not exist in the table 'ratings':
 * <ul>
 * <li>a new item must be created in the table 'user_ratings'</li>
 * <li>its version is set to 1</li>
 * <li>a 'v0-' must be appended to the attribute 'user', meaning it's the
 * current version</li>
 * <li>a new item must be created in the table 'ratings'</li>
 * </ul>
 * When a user rating is updated for a code that does not exist in the table
 * 'ratings' (this should never happen):
 * <ul>
 * <li>a new item must be created in the table 'user_ratings'</li>
 * <li>its attributes should be identical to the previous item's, except 'v0-'
 * must be updated to that item's version</li>
 * <li>the current item ('v0-' appended to 'user') must be updated with the new
 * attributes</li>
 * <li>its version must be set to previous item's version plus 1</li>
 * <li>a new item must be created in the table 'ratings'</li>
 * </ul>
 * When a new user rating is created for a code that already exists in the table
 * 'ratings':
 * <ul>
 * <li>a new item must be created in the table 'user_ratings'</li>
 * <li>its version is set to 1</li>
 * <li>a 'v0-' must be appended to the attribute 'user', meaning it's the
 * current version</li>
 * <li>a new item must be created in the table 'ratings'</li>
 * <li>its attribute aggregated must be updated according to the difference
 * between the current and previous versions of user rating</li>
 * </ul>
 * When a user rating is updated for a code that already exists in the table
 * 'ratings':
 * <ul>
 * <li>a new item must be created in the table 'user_ratings'</li>
 * <li>its attributes should be identical to the previous item's, except 'v0-'
 * must be updated to that item's version</li>
 * <li>the current item ('v0-' appended to 'user') must be updated with the new
 * attributes</li>
 * <li>its version must be set to previous item's version plus 1</li>
 * <li>a new item must be created in the table 'ratings'</li>
 * <li>its attribute aggregated must be updated according to the difference
 * between the current and previous versions of user rating</li>
 * </ul>
 */
@ExtendWith({ MockitoExtension.class, DynamoDBExtension.class })
class DynamoDbUserRatingServiceIntegrationTest {

	private static final String TABLE_NAME_USER_RATINGS = "user_ratings";
	private static final String USER_RATING_SKU = "sku";
	private static final String USER_RATING_USER = "user";
	private static final String USER_RATING_RATING = "rating";
	private static final String USER_RATING_DATE = "date";
	private static final String USER_RATING_REVIEW = "review";
	private static final String USER_RATING_VERSION = "version";
	private static final String GSI_RATING = "GSIRating";
	private static final String SKU_VALUE = "1321123";
	private static final String USER_VALUE_NO_VERSION = "122151";
	private static final String SKU2_VALUE = "78474577";
	private static final String USER2_VALUE_NO_VERSION = "87548714";
	private static final String USER_VALUE_1 = "v1-122151";
	private static final Long VERSION_VALUE_1 = 1L;
	private static final String REVIEW_VALUE_1 = "Terrible";
	private static final Long DATE_VALUE_1 = 3L;
	private static final Long RATING_VALUE_1 = 3L;
	private static final String USER_VALUE_2 = "v2-122151";
	private static final Long VERSION_VALUE_2 = 2L;
	private static final String REVIEW_VALUE_2 = "Not too terrible";
	private static final Long DATE_VALUE_2 = 8L;
	private static final Long RATING_VALUE_2 = 6L;
	private static final String USER_VALUE_CURRENT = "v0-122151";
	private static final Long VERSION_VALUE_3 = 3L;
	private static final String REVIEW_VALUE_3 = "Neat";
	private static final Long DATE_VALUE_3 = 17L;
	private static final Long RATING_VALUE_3 = 9L;
	private static final Long NEW_DATE = 1555L;
	private static final Long NEW_RATING = 1L;
	private static final String NEW_REVIEW = "Smells funny";

	private static final String TABLE_NAME_RATINGS = "ratings";
	private static final String RATING_SKU = "sku";
	private static final String RATING_DATE = "date";
	private static final String RATING_QUANTITY = "quantity";
	private static final String RATING_AGGREGATED = "aggregated";
	private static final String RATING_VERSION = "version";
	private static final String LSI_DATE = "LSIDate";

	private DynamoDbClient client;
	private DynamoDbUserRatingService service;

	/*
	 * AttributeDefinitions: - AttributeName: sku AttributeType: S # Most recent
	 * reviews will have user starting with 'v0-', this will make sorting a lot
	 * easier and cheaper - AttributeName: user AttributeType: S - AttributeName:
	 * rating AttributeType: N - AttributeName: date AttributeType: N -
	 * AttributeName: review AttributeType: S - AttributeName: version
	 * AttributeType: N KeySchema: - AttributeName: sku KeyType: HASH -
	 * AttributeName: user KeyType: RANGE
	 */
	@BeforeEach
	public void setUp() throws InterruptedException, ExecutionException {
		client = DynamoDbClient.builder().region(Region.US_EAST_1)
				.endpointOverride(URI.create("http://localhost:" + DynamoDBExtension.SERVER_PORT)).build();

		service = new DynamoDbUserRatingService(client).withUserRatingsTable(TABLE_NAME_USER_RATINGS);

		createTableUserRatings();
		createTableRatings();
		ListTablesResponse listTablesResponseAfterCreation = client.listTables();
		System.out.println("Created table:");
		listTablesResponseAfterCreation.tableNames().stream().forEach(System.out::println);
		pupulateTableUserRatings();
		pupulateTableRatings();
	}

	@AfterEach
	public void tearDown() throws InterruptedException, ExecutionException {
		// get table names
		ListTablesResponse listTablesResponseAfterCreation = client.listTables();
		// delete table found
		listTablesResponseAfterCreation.tableNames().stream()
				.map(name -> DeleteTableRequest.builder().tableName(name).build()).forEach(client::deleteTable);
	}

	@Test
	void testGetUserReturnsLatestVersionFromTable() {
		// given table is populated with the initial static data

		// when
		UserRating rating = service.getByUserIdAndCode(USER_VALUE_NO_VERSION, SKU_VALUE);

		// then
		assertEquals(SKU_VALUE, rating.getSku());
		assertEquals(USER_VALUE_CURRENT, rating.getUser());
		assertEquals(DATE_VALUE_3, rating.getDate());
		assertEquals(RATING_VALUE_3, rating.getRating());
		assertEquals(REVIEW_VALUE_3, rating.getReview());
		assertEquals(VERSION_VALUE_3, rating.getVersion());
	}

	@Test
	void testGetItemReturnsSpecificItemFromTable() {
		// given
		String code = SKU_VALUE;
		String userId = USER_VALUE_2;

		// when
		UserRating rating = service.getItem(code, userId);
		// then
		assertEquals(SKU_VALUE, rating.getSku());
		assertEquals(USER_VALUE_2, rating.getUser());
		assertEquals(DATE_VALUE_2, rating.getDate());
		assertEquals(RATING_VALUE_2, rating.getRating());
		assertEquals(REVIEW_VALUE_2, rating.getReview());
		assertEquals(VERSION_VALUE_2, rating.getVersion());
	}

	@Test
	void testDatabaseExceptionIsThrownOnGetItemException() {
		// given
		DynamoDbClient mockClient = Mockito.mock(DynamoDbClient.class);
		service = new DynamoDbUserRatingService(mockClient).withUserRatingsTable(TABLE_NAME_USER_RATINGS);
		doThrow(new RuntimeException()).when(mockClient).getItem(Mockito.<GetItemRequest>any());

		// then
		assertThrows(DatabaseException.class, () -> {
			// when
			service.getByUserIdAndCode(USER_VALUE_NO_VERSION, SKU_VALUE);
		});
	}

	@Test
	void testGetUserReturnsNullIfNoItemIsFound() {
		// given table is populated with the initial static data

		// when
		UserRating rating = service.getByUserIdAndCode("xyz", "3");

		// then
		assertNull(rating);
	}

	@Test
	void testPutUserRatingCreatesNewItem_WhenUserRatingDoesNotExist() {
		// given
		UserRating userRating = createUserRating(SKU_VALUE, USER2_VALUE_NO_VERSION, NEW_DATE, NEW_RATING, NEW_REVIEW,
				1000L); // version shouldn't be taken into account

		// when
		service.put(userRating);

		// then
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER,
				"v0-" + USER2_VALUE_NO_VERSION, USER_RATING_DATE, NEW_DATE);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER,
				"v0-" + USER2_VALUE_NO_VERSION, USER_RATING_RATING, NEW_RATING);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER,
				"v0-" + USER2_VALUE_NO_VERSION, USER_RATING_REVIEW, NEW_REVIEW);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER,
				"v0-" + USER2_VALUE_NO_VERSION, USER_RATING_VERSION, 1L);
	}

	@Test
	void testPutUserRatingReturnsCreatedNewItem_WhenUserRatingDoesNotExist() {
		// given
		UserRating userRating = createUserRating(SKU_VALUE, USER2_VALUE_NO_VERSION, NEW_DATE, NEW_RATING, NEW_REVIEW,
				1000L); // version shouldn't be taken into account

		// when
		UserRating item = service.put(userRating);

		// then
		assertEquals(SKU_VALUE, item.getSku());
		assertEquals(USER2_VALUE_NO_VERSION, item.getUser());
		assertEquals(NEW_DATE, item.getDate());
		assertEquals(NEW_RATING, item.getRating());
		assertEquals(NEW_REVIEW, item.getReview());
		assertEquals(1L, item.getVersion());
	}

	/*
	 * When an existing rating is updated, the v0- must be set with attributes from
	 * the new item and its version should be incremented
	 */
	@Test
	void testPutUserRatingUpdatesCurrentVersionInTable_WhenUserRatingAlreadyExists() {
		// given
		UserRating userRating = createUserRating(SKU_VALUE, USER_VALUE_NO_VERSION, NEW_DATE, NEW_RATING, NEW_REVIEW,
				1000L); // version shouldn't be taken into account

		// when
		service.put(userRating);

		// then
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER, USER_VALUE_CURRENT,
				USER_RATING_DATE, NEW_DATE);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER, USER_VALUE_CURRENT,
				USER_RATING_RATING, NEW_RATING);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER, USER_VALUE_CURRENT,
				USER_RATING_REVIEW, NEW_REVIEW);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER, USER_VALUE_CURRENT,
				USER_RATING_VERSION, VERSION_VALUE_3 + 1);
	}

	/*
	 * When an existing rating is updated, a new item must be created in the table
	 * 'user_ratings' and its attributes should be identical to the previous item's,
	 * except 'v0-' must be updated with last version
	 */
	@Test
	void testPutUserRatingCreatesItemForPreviousVersionInTable_WhenUserRatingAlreadyExists() {
		// given
		UserRating userRating = createUserRating(SKU_VALUE, USER_VALUE_NO_VERSION, NEW_DATE, NEW_RATING, NEW_REVIEW,
				1000L); // version shouldn't be taken into account

		// when
		service.put(userRating);

		// then
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER,
				"v3-" + USER_VALUE_NO_VERSION, USER_RATING_DATE, DATE_VALUE_3);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER,
				"v3-" + USER_VALUE_NO_VERSION, USER_RATING_RATING, RATING_VALUE_3);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER,
				"v3-" + USER_VALUE_NO_VERSION, USER_RATING_REVIEW, REVIEW_VALUE_3);
		assertAttribute(TABLE_NAME_USER_RATINGS, USER_RATING_SKU, SKU_VALUE, USER_RATING_USER,
				"v3-" + USER_VALUE_NO_VERSION, USER_RATING_VERSION, VERSION_VALUE_3);
	}

	@Test
	void testPutUserRatingCreatesNewItemInRatingsTableWithSameQuantityAndNewAggregatedAndNewDateAndNewVersion_WhenUserRatingAlreadyExistsAndRatingAlreadyExists() {
		// given
		UserRating userRating = createUserRating(SKU_VALUE, USER_VALUE_NO_VERSION, NEW_DATE, NEW_RATING, NEW_REVIEW,
				null);

		// when
		service.put(userRating);

		// then
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_QUANTITY, 1L);
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_AGGREGATED,
				NEW_RATING);
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_VERSION,
				VERSION_VALUE_3 + 1);
	}

	@Test
	void testPutUserRatingCreatesNewItemInRatingsTableWithIncrementedQuantityAndNewAggregatedAndNewDateAndNewVersion_WhenUserRatingIsNewAndRatingAlreadyExists() {
		// given
		UserRating userRating = createUserRating(SKU_VALUE, USER2_VALUE_NO_VERSION, NEW_DATE, NEW_RATING, NEW_REVIEW,
				null);

		// when
		service.put(userRating);

		// then
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_QUANTITY, 2L);
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_AGGREGATED,
				NEW_RATING + RATING_VALUE_3); // aggregated is new_rating (new value passed in put request) + value_3
												// (value for the existing item)
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_VERSION,
				VERSION_VALUE_3 + 1);
	}

	@Test
	void testPutUserRatingUpdatesRatingsTableWithNewQuantityAndNewAggregatedAndNewDateAndNewVersion_WhenUserRatingAlreadyExistsButRatingIsNew()
			throws InterruptedException, ExecutionException {
		// given
		UserRating userRating = createUserRating(SKU2_VALUE, USER_VALUE_NO_VERSION, NEW_DATE, NEW_RATING, NEW_REVIEW,
				null);

		// when an existing user rating is updated
		service.put(userRating);

		// then
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU2_VALUE, RATING_DATE, NEW_DATE, RATING_QUANTITY, 1L);
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU2_VALUE, RATING_DATE, NEW_DATE, RATING_AGGREGATED,
				NEW_RATING);
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU2_VALUE, RATING_DATE, NEW_DATE, RATING_VERSION, 1);
	}

	@Test
	void testPutUserRatingUpdatesRatingsTableWithNewQuantityAndNewAggregatedAndNewDateAndNewVersion_WhenUserRatingIsNewAndRatingIsNew()
			throws InterruptedException, ExecutionException {
		// given
		// there is no items in the ratings table, for some reason, but there are user
		// ratings
		client.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME_RATINGS).build());
		createTableRatings();
		UserRating userRating = createUserRating(SKU_VALUE, USER_VALUE_NO_VERSION, NEW_DATE, NEW_RATING, NEW_REVIEW,
				null);

		// when an existing user rating is updated
		service.put(userRating);

		// then
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_QUANTITY, 1L);
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_AGGREGATED,
				NEW_RATING);
		assertAttribute(TABLE_NAME_RATINGS, RATING_SKU, SKU_VALUE, RATING_DATE, NEW_DATE, RATING_VERSION, 1);
	}

	@Test
	void testDatabaseExceptionIsThrownOnPutItemException() {
		// given
		DynamoDbUserRatingService spyService = Mockito.spy(service);
		doThrow(new RuntimeException()).when(spyService).getItem(any(), any());
		UserRating userRating = new UserRating();

		// then
		assertThrows(DatabaseException.class, () -> {
			// when
			spyService.put(userRating);
		});
	}

	private UserRating createUserRating(String code, String user, Long date, Long rating, String review, Long version) {
		UserRating userRating = new UserRating();
		userRating.setDate(date);
		userRating.setRating(rating);
		userRating.setSku(code);
		userRating.setReview(review);
		userRating.setUser(user);
		userRating.setVersion(version);
		return userRating;
	}

	private void createTableUserRatings() throws InterruptedException, ExecutionException {
		client.createTable(CreateTableRequest.builder().tableName(TABLE_NAME_USER_RATINGS)
				.attributeDefinitions(
						AttributeDefinition.builder().attributeName(USER_RATING_SKU).attributeType("S").build(),
						AttributeDefinition.builder().attributeName(USER_RATING_USER).attributeType("S").build(),
						AttributeDefinition.builder().attributeName(USER_RATING_RATING).attributeType("N").build())
				.keySchema(KeySchemaElement.builder().attributeName(USER_RATING_SKU).keyType("HASH").build(),
						KeySchemaElement.builder().attributeName(USER_RATING_USER).keyType("RANGE").build())
				.globalSecondaryIndexes(GlobalSecondaryIndex.builder().indexName(GSI_RATING)
						.keySchema(KeySchemaElement.builder().attributeName(USER_RATING_USER).keyType("HASH").build(),
								KeySchemaElement.builder().attributeName(USER_RATING_RATING).keyType("RANGE").build())
						.projection(Projection.builder().projectionType("INCLUDE")
								.nonKeyAttributes(USER_RATING_SKU, USER_RATING_DATE).build())
						.provisionedThroughput(
								ProvisionedThroughput.builder().readCapacityUnits(10l).writeCapacityUnits(10l).build())
						.build())
				.provisionedThroughput(
						ProvisionedThroughput.builder().readCapacityUnits(10l).writeCapacityUnits(10l).build())
				.build());
	}

	private void createTableRatings() throws InterruptedException, ExecutionException {
		client.createTable(CreateTableRequest.builder().tableName(TABLE_NAME_RATINGS)
				.attributeDefinitions(
						AttributeDefinition.builder().attributeName(RATING_SKU).attributeType("S").build(),
						AttributeDefinition.builder().attributeName(RATING_DATE).attributeType("N").build())
				.keySchema(KeySchemaElement.builder().attributeName(RATING_SKU).keyType("HASH").build(),
						KeySchemaElement.builder().attributeName(RATING_DATE).keyType("RANGE").build())
				.localSecondaryIndexes(LocalSecondaryIndex.builder().indexName(LSI_DATE)
						.keySchema(KeySchemaElement.builder().attributeName(RATING_SKU).keyType("HASH").build(),
								KeySchemaElement.builder().attributeName(RATING_DATE).keyType("RANGE").build())
						.projection(Projection.builder().projectionType("ALL").build()).build())
				.provisionedThroughput(
						ProvisionedThroughput.builder().readCapacityUnits(10l).writeCapacityUnits(10l).build())
				.build());
	}

	private void pupulateTableUserRatings() throws InterruptedException, ExecutionException {
		Map<String, Collection<WriteRequest>> requestItems = new HashMap<>();
		Collection<WriteRequest> items = new ArrayList<>(3);
		items.add(createUserRatingPutRequest(SKU_VALUE, USER_VALUE_1, RATING_VALUE_1, DATE_VALUE_1, REVIEW_VALUE_1,
				VERSION_VALUE_1));
		items.add(createUserRatingPutRequest(SKU_VALUE, USER_VALUE_2, RATING_VALUE_2, DATE_VALUE_2, REVIEW_VALUE_2,
				VERSION_VALUE_2));
		items.add(createUserRatingPutRequest(SKU_VALUE, USER_VALUE_CURRENT, RATING_VALUE_3, DATE_VALUE_3,
				REVIEW_VALUE_3, VERSION_VALUE_3)); // current one - v0
		requestItems.put(TABLE_NAME_USER_RATINGS, items);
		BatchWriteItemRequest request = BatchWriteItemRequest.builder().requestItems(requestItems).build();
		BatchWriteItemResponse result = client.batchWriteItem(request);
		System.out.println("Table " + TABLE_NAME_USER_RATINGS + " fully populated: " + result.hasUnprocessedItems());
	}

	private void pupulateTableRatings() throws InterruptedException, ExecutionException {
		Map<String, Collection<WriteRequest>> requestItems = new HashMap<>();
		Collection<WriteRequest> items = new ArrayList<>(3);
		items.add(createRatingPutRequest(SKU_VALUE, DATE_VALUE_1, 1L, RATING_VALUE_1, VERSION_VALUE_1));
		items.add(createRatingPutRequest(SKU_VALUE, DATE_VALUE_2, 1L, RATING_VALUE_2, VERSION_VALUE_2));
		items.add(createRatingPutRequest(SKU_VALUE, DATE_VALUE_3, 1L, RATING_VALUE_3, VERSION_VALUE_3));
		requestItems.put(TABLE_NAME_RATINGS, items);
		BatchWriteItemRequest request = BatchWriteItemRequest.builder().requestItems(requestItems).build();
		BatchWriteItemResponse result = client.batchWriteItem(request);
		System.out.println("Table " + TABLE_NAME_RATINGS + " fully populated: " + result.hasUnprocessedItems());
	}

	private WriteRequest createUserRatingPutRequest(String skuValue, String userValue, Long ratingValue, Long dateValue,
			String reviewValue, Long versionValue) {
		Map<String, AttributeValue> attributeMap = new HashMap<>();
		attributeMap.put(USER_RATING_SKU, AttributeValue.builder().s(skuValue).build());
		attributeMap.put(USER_RATING_USER, AttributeValue.builder().s(userValue).build());
		attributeMap.put(USER_RATING_RATING, AttributeValue.builder().n(String.valueOf(ratingValue)).build());
		attributeMap.put(USER_RATING_DATE, AttributeValue.builder().n(String.valueOf(dateValue)).build());
		attributeMap.put(USER_RATING_REVIEW, AttributeValue.builder().s(reviewValue).build());
		attributeMap.put(USER_RATING_VERSION, AttributeValue.builder().n(String.valueOf(versionValue)).build());
		PutRequest putRequest = PutRequest.builder().item(attributeMap).build();
		WriteRequest userRating = WriteRequest.builder().putRequest(putRequest).build();
		return userRating;
	}

	private WriteRequest createRatingPutRequest(String skuValue, Long dateValue, Long quantity, Long aggregated,
			Long version) {
		Map<String, AttributeValue> attributeMap = new HashMap<>();
		attributeMap.put(RATING_SKU, AttributeValue.builder().s(skuValue).build());
		attributeMap.put(RATING_DATE, AttributeValue.builder().n(String.valueOf(dateValue)).build());
		attributeMap.put(RATING_QUANTITY, AttributeValue.builder().n(String.valueOf(quantity)).build());
		attributeMap.put(RATING_AGGREGATED, AttributeValue.builder().n(String.valueOf(aggregated)).build());
		attributeMap.put(RATING_VERSION, AttributeValue.builder().n(String.valueOf(version)).build());
		PutRequest putRequest = PutRequest.builder().item(attributeMap).build();
		WriteRequest userRating = WriteRequest.builder().putRequest(putRequest).build();
		return userRating;
	}

	private void assertAttribute(String tableName, String hash, String hashValue, String sort, Object sortValue,
			String attributeName, Object attributeValue) {
		Map<String, AttributeValue> key = new HashMap<>();
		key.put(hash, AttributeValue.fromS(hashValue));
		key.put(sort, sortValue instanceof Long ? AttributeValue.fromN(String.valueOf(sortValue))
				: AttributeValue.fromS((String) sortValue));
		GetItemResponse itemResponse = client
				.getItem(GetItemRequest.builder().tableName(tableName).key(key).attributesToGet(attributeName).build());
		assertEquals(attributeValue.toString(), getFirst(itemResponse.item().get(attributeName)));
	}

	private Object getFirst(AttributeValue value) {
		return value == null ? null : value.s() != null ? value.s() : value.n() != null ? value.n() : value.b();
	}
}
