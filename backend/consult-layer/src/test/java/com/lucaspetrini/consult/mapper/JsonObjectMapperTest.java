package com.lucaspetrini.consult.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;

/**
 * Test {@link JsonObjectMapper}.
 */
public class JsonObjectMapperTest {
	private static final String CODE = "SKU123";
	private static final String USER = "mrpickles";
	private static final Long RATING = 3L;
	private static final String REVIEW = "Good";
	private static final String SERIALISED_PUT_REQUEST = "{'rating':3,'date':9941,'review':'Good'}".replace("'", "\"");
	private static final String DESERIALISED_GET_RESPONSE = "{'code':'SKU123','user':'mrpickles','rating':3,'date':9941,'review':'Good'}".replace("'", "\"");
	private static final Long DATE = 9941L;
	private JsonObjectMapper mapper;

	@BeforeEach
	public void setUp() {
		mapper = new JsonObjectMapper();
	}

	@Test
	public void testPutRequestIsSerialised() {
		PutUserRatingRequest request = new PutUserRatingRequest();
		request.setRating(RATING);
		request.setDate(DATE);
		request.setReview(REVIEW);

		String serialisedRequest = mapper.serialise(request);

		assertEquals(SERIALISED_PUT_REQUEST, serialisedRequest);
	}

	@Test
	public void testGetResponseIsDeserialised() {
		GetUserRatingResponse response = mapper.deserialise(DESERIALISED_GET_RESPONSE, GetUserRatingResponse.class);

		assertEquals(CODE, response.getCode());
		assertEquals(USER, response.getUser());
		assertEquals(RATING, response.getRating());
		assertEquals(DATE, response.getDate());
		assertEquals(REVIEW, response.getReview());
	}

}
