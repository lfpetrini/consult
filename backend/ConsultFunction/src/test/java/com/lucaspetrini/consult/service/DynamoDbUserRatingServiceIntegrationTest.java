package com.lucaspetrini.consult.service;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucaspetrini.consult.utils.DynamoDBExtension;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

/**
 * Tests for {@link DynamoDbUserRatingService}.
 */
@ExtendWith({ MockitoExtension.class, DynamoDBExtension.class })
class DynamoDbUserRatingServiceIntegrationTest {

	private static final String TABLE_NAME_USER_RATING = "user_rating";
	private static final String USER_RATING_SKU = "sku";
	private static final String USER_RATING_USER = "user";
	private static final String USER_RATING_RATING = "rating";
	private static final String USER_RATING_DATE = "date";
	private static final String USER_RATING_REVIEW = "review";
	private static final String USER_RATING_VERSION = "version";
	private static final String GSI_RATING = "GSIRating";
	private static final String SKU_VALUE = "1321123";
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
	private static final String USER_VALUE_3 = "v0-122151";
	private static final Long VERSION_VALUE_3 = 3L;
	private static final String REVIEW_VALUE_3 = "Neat";
	private static final Long DATE_VALUE_3 = 17L;
	private static final Long RATING_VALUE_3 = 9L;

	private DynamoDbAsyncClient client;
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
		client = DynamoDbAsyncClient.builder().region(Region.US_EAST_1)
				.endpointOverride(URI.create("http://localhost:" + DynamoDBExtension.SERVER_PORT)).build();

		service = new DynamoDbUserRatingService().withDynamoDbAsyncClient(client);

		createTableUserRating();
		pupulateTableUserRating();

		ListTablesResponse listTablesResponseAfterCreation = client.listTables().get();
		listTablesResponseAfterCreation.tableNames().stream().forEach(System.out::println);
	}

	private void createTableUserRating() throws InterruptedException, ExecutionException {
		client.createTable(CreateTableRequest.builder().tableName(TABLE_NAME_USER_RATING)
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
								.nonKeyAttributes(USER_RATING_SKU, USER_RATING_DATE, USER_RATING_REVIEW).build())
						.provisionedThroughput(
								ProvisionedThroughput.builder().readCapacityUnits(10l).writeCapacityUnits(10l).build())
						.build())
				.provisionedThroughput(
						ProvisionedThroughput.builder().readCapacityUnits(10l).writeCapacityUnits(10l).build())
				.build()).get();
	}

	private void pupulateTableUserRating() throws InterruptedException, ExecutionException {
		Map<String, Collection<WriteRequest>> requestItems = new HashMap<>();
		Collection<WriteRequest> items = new ArrayList<>(3);
		items.add(createUserRatingPutRequest(SKU_VALUE, USER_VALUE_1, RATING_VALUE_1, DATE_VALUE_1, REVIEW_VALUE_1, VERSION_VALUE_1));
		items.add(createUserRatingPutRequest(SKU_VALUE, USER_VALUE_3, RATING_VALUE_3, DATE_VALUE_3, REVIEW_VALUE_3, VERSION_VALUE_3)); // current one - v0
		items.add(createUserRatingPutRequest(SKU_VALUE, USER_VALUE_2, RATING_VALUE_2, DATE_VALUE_2, REVIEW_VALUE_2, VERSION_VALUE_2));
		requestItems.put(TABLE_NAME_USER_RATING, items);
		BatchWriteItemRequest request = BatchWriteItemRequest.builder().requestItems(requestItems).build();
		BatchWriteItemResponse result = client.batchWriteItem(request).get();
		System.out.println("Table fully populated: " + result.hasUnprocessedItems());
	}

	private WriteRequest createUserRatingPutRequest(String skuValue, String userValue, Long ratingValue,
			Long dateValue, String reviewValue, Long versionValue) {
		Map<String, AttributeValue> attributeMap = new HashMap<>();
		attributeMap.put(USER_RATING_SKU, AttributeValue.builder().s(skuValue).build());
		attributeMap.put(USER_RATING_USER, AttributeValue.builder().s(userValue).build());
		attributeMap.put(USER_RATING_RATING, AttributeValue.builder().n(String.valueOf(ratingValue)).build());
		attributeMap.put(USER_RATING_DATE, AttributeValue.builder().n(String.valueOf(dateValue)).build());
		attributeMap.put(USER_RATING_REVIEW, AttributeValue.builder().s(reviewValue).build());
		attributeMap.put(USER_RATING_VERSION, AttributeValue.builder().n(String.valueOf(versionValue)).build());
		PutRequest putRequest = PutRequest.builder().item(attributeMap).build();
		WriteRequest userRating = WriteRequest.builder().putRequest(putRequest ).build();
		return userRating;
	}

	@AfterEach
	public void tearDown() {
		client.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME_USER_RATING).build());
	}

	@Test
	void testGetUserReturnsLatestVersionFromTable() {
		fail("Not yet implemented");
	}

	@Test
	void testDatabaseExceptionIsThrownOnGetItemException() {
		fail("Not yet implemented");
	}

	@Test
	void testGetUserReturnsNullIfNoItemIsFound() {
		fail("Not yet implemented");
	}

}
