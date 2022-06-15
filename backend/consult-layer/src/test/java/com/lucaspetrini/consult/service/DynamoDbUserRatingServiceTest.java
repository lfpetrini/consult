package com.lucaspetrini.consult.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lucaspetrini.consult.service.model.UserRating;

/**
 * Tests for {@link DynamoDbUserRatingService}.
 */
class DynamoDbUserRatingServiceTest {
	private static final Long VERSION = 13L;
	private static final String REVIEW = "Hello world";
	private static final Long RATING = 7L;
	private static final Long DATE = 15165261L;
	private static final String USER = "us3r";
	private static final String SKU = "5ku";
	private DynamoDbUserRatingService service;

	@BeforeEach
	public void setUp() throws InterruptedException, ExecutionException {
		service = new DynamoDbUserRatingService();
	}

	@Test
	void testObjectReturnedByCloneUserRatingIsIdenticalToObjectPassedAsParameter() {
		// given
		UserRating userRating = createUserRating(SKU, USER, DATE, RATING, REVIEW, VERSION);
		
		// when
		UserRating newUserRating = service.cloneUserRating(userRating);
		
		// then
		assertEquals(SKU, newUserRating.getSku());
		assertEquals(USER, newUserRating.getUser());
		assertEquals(DATE, newUserRating.getDate());
		assertEquals(RATING, newUserRating.getRating());
		assertEquals(REVIEW, newUserRating.getReview());
		assertEquals(VERSION, newUserRating.getVersion());
	}

	@Test
	void testObjectReturnedByCloneUserRatingIsNotTheSameAsTheObjectPassedAsParameter() {
		// given
		UserRating userRating = createUserRating(SKU, USER, DATE, RATING, REVIEW, VERSION);
		
		// when
		UserRating newUserRating = service.cloneUserRating(userRating);
		
		// then
		assertNotSame(userRating, newUserRating);
	}

	@Test
	void testNullIsReturnedByCloneWhenNullIsPassedAsParameter() {
		// given
		UserRating userRating = null;
		
		// when
		UserRating newUserRating = service.cloneUserRating(userRating);
		
		// then
		assertNull(newUserRating);
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
}
