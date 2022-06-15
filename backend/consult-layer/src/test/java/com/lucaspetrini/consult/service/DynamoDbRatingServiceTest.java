package com.lucaspetrini.consult.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lucaspetrini.consult.service.model.Rating;

/**
 * Tests for {@link DynamoDbRatingService}.
 */
class DynamoDbRatingServiceTest {
	private static final Long VERSION = 13L;
	private static final Long AGGREGATED = 19L;
	private static final Long QUANTITY = 7L;
	private static final Long DATE = 15165261L;
	private static final String SKU = "5ku";
	private DynamoDbRatingService service;

	@BeforeEach
	public void setUp() throws InterruptedException, ExecutionException {
		service = new DynamoDbRatingService();
	}

	@Test
	void testObjectReturnedByCloneRatingIsIdenticalToObjectPassedAsParameter() {
		// given
		Rating rating = createRating(SKU, DATE, QUANTITY, AGGREGATED, VERSION);
		
		// when
		Rating newRating = service.cloneRating(rating);
		
		// then
		assertEquals(SKU, newRating.getSku());
		assertEquals(DATE, newRating.getDate());
		assertEquals(QUANTITY, newRating.getQuantity());
		assertEquals(AGGREGATED, newRating.getAggregated());
		assertEquals(VERSION, newRating.getVersion());
	}

	@Test
	void testObjectReturnedByCloneRatingIsNotTheSameAsTheObjectPassedAsParameter() {
		// given
		Rating rating = createRating(SKU, DATE, QUANTITY, AGGREGATED, VERSION);
		
		// when
		Rating newRating = service.cloneRating(rating);
		
		// then
		assertNotSame(rating, newRating);
	}

	@Test
	void testNullIsReturnedByCloneWhenNullIsPassedAsParameter() {
		// given
		Rating rating = null;
		
		// when
		Rating newRating = service.cloneRating(rating);
		
		// then
		assertNull(newRating);
	}

	private Rating createRating(String code, Long date, Long quantity, Long aggregated, Long version) {
		Rating rating = new Rating();
		rating.setDate(date);
		rating.setQuantity(quantity);
		rating.setSku(code);
		rating.setAggregated(aggregated);
		rating.setVersion(version);
		return rating;
	}
}
