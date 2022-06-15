package com.lucaspetrini.consult.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

/**
 * Tests for {@link DynamoDbRatingService}.
 */
@ExtendWith({ MockitoExtension.class, DynamoDBExtension.class })
class DynamoDbRatingServiceIntegrationTest {
	private static final String SKU_VALUE = "1321123";
	private static final Long VERSION_VALUE_1 = 1L;
	private static final Long AGGREGATED_VALUE_1 = 39L;
	private static final Long DATE_VALUE_1 = 3L;
	private static final Long QUANTITY_VALUE_1 = 7L;
	private static final Long VERSION_VALUE_2 = 2L;
	private static final Long AGGREGATED_VALUE_2 = 93L;
	private static final Long DATE_VALUE_2 = 8L;
	private static final Long QUANTITY_VALUE_2 = 11L;
	private static final Long VERSION_VALUE_3 = 3L;
	private static final Long DATE_VALUE_3 = 17L;
	private static final Long AGGREGATED_VALUE_3 = 171L;
	private static final Long QUANTITY_VALUE_3 = 19L;
	private static final String TABLE_NAME_RATINGS = "ratings";
	private static final String RATING_SKU = "sku";
	private static final String RATING_DATE = "date";
	private static final String RATING_QUANTITY = "quantity";
	private static final String RATING_AGGREGATED = "aggregated";
	private static final String RATING_VERSION = "version";
	private static final String LSI_DATE = "LSIDate";

	private DynamoDbClient client;
	private DynamoDbRatingService service;

	@BeforeEach
	public void setUp() throws InterruptedException, ExecutionException {
		client = DynamoDbClient.builder().region(Region.US_EAST_1)
				.endpointOverride(URI.create("http://localhost:" + DynamoDBExtension.SERVER_PORT)).build();

		service = new DynamoDbRatingService(client).withRatingsTable(TABLE_NAME_RATINGS);

		createTableRatings();
		ListTablesResponse listTablesResponseAfterCreation = client.listTables();
		System.out.println("Created table:");
		listTablesResponseAfterCreation.tableNames().stream().forEach(System.out::println);
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
	void testGetRatingReturnsLatestVersionFromTable() {
		// given table is populated with the initial static data

		// when
		Rating rating = service.getByCode(SKU_VALUE);

		// then
		assertEquals(SKU_VALUE, rating.getSku());
		assertEquals(DATE_VALUE_3, rating.getDate());
		assertEquals(AGGREGATED_VALUE_3, rating.getAggregated());
		assertEquals(QUANTITY_VALUE_3, rating.getQuantity());
		assertEquals(VERSION_VALUE_3, rating.getVersion());
	}

	@Test
	void testGetItemReturnsSpecificItemFromTable() throws InterruptedException, ExecutionException {
		// given
		String code = SKU_VALUE;
		Long version = VERSION_VALUE_2;

		// when
		Rating rating = service.getItem(code, version);
		// then
		assertEquals(SKU_VALUE, rating.getSku());
		assertEquals(DATE_VALUE_2, rating.getDate());
		assertEquals(AGGREGATED_VALUE_2, rating.getAggregated());
		assertEquals(QUANTITY_VALUE_2, rating.getQuantity());
		assertEquals(VERSION_VALUE_2, rating.getVersion());
	}

	@Test
	void testDatabaseExceptionIsThrownOnGetItemException() {
		// given
		DynamoDbClient mockClient = Mockito.mock(DynamoDbClient.class);
		service = new DynamoDbRatingService(mockClient).withRatingsTable(TABLE_NAME_RATINGS);
		//doThrow(new RuntimeException()).when(mockClient).getItem(Mockito.<GetItemRequest>any());
		// no need to stub, a mock will throw a null pointer exception anyway

		// then
		assertThrows(DatabaseException.class, () -> {
			// when
			service.getByCode(SKU_VALUE);
		});
	}

	@Test
	void testGetRatingReturnsNullIfNoItemIsFound() {
		// given table is populated with the initial static data

		// when
		Rating rating = service.getByCode("xyz");

		// then
		assertNull(rating);
	}

	private void createTableRatings() throws InterruptedException, ExecutionException {
		client.createTable(CreateTableRequest.builder().tableName(TABLE_NAME_RATINGS)
				.attributeDefinitions(
						AttributeDefinition.builder().attributeName(RATING_SKU).attributeType("S").build(),
						AttributeDefinition.builder().attributeName(RATING_VERSION).attributeType("N").build(),
						AttributeDefinition.builder().attributeName(RATING_DATE).attributeType("N").build())
				.keySchema(KeySchemaElement.builder().attributeName(RATING_SKU).keyType("HASH").build(),
						KeySchemaElement.builder().attributeName(RATING_VERSION).keyType("RANGE").build())
				.localSecondaryIndexes(LocalSecondaryIndex.builder().indexName(LSI_DATE)
						.keySchema(KeySchemaElement.builder().attributeName(RATING_SKU).keyType("HASH").build(),
								KeySchemaElement.builder().attributeName(RATING_DATE).keyType("RANGE").build())
						.projection(Projection.builder().projectionType("ALL").build()).build())
				.provisionedThroughput(
						ProvisionedThroughput.builder().readCapacityUnits(10l).writeCapacityUnits(10l).build())
				.build());
	}

	private void pupulateTableRatings() throws InterruptedException, ExecutionException {
		Map<String, Collection<WriteRequest>> requestItems = new HashMap<>();
		Collection<WriteRequest> items = new ArrayList<>(3);
		items.add(createRatingPutRequest(SKU_VALUE, DATE_VALUE_1, QUANTITY_VALUE_1, AGGREGATED_VALUE_1, VERSION_VALUE_1));
		items.add(createRatingPutRequest(SKU_VALUE, DATE_VALUE_2, QUANTITY_VALUE_2, AGGREGATED_VALUE_2, VERSION_VALUE_2));
		items.add(createRatingPutRequest(SKU_VALUE, DATE_VALUE_3, QUANTITY_VALUE_3, AGGREGATED_VALUE_3, VERSION_VALUE_3));
		requestItems.put(TABLE_NAME_RATINGS, items);
		BatchWriteItemRequest request = BatchWriteItemRequest.builder().requestItems(requestItems).build();
		BatchWriteItemResponse result = client.batchWriteItem(request);
		System.out.println("Table " + TABLE_NAME_RATINGS + " fully populated: " + result.hasUnprocessedItems());
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
		WriteRequest rating = WriteRequest.builder().putRequest(putRequest).build();
		return rating;
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
