package com.lucaspetrini.consult.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test {@link JsonObjectMapper}.
 */
public class JsonObjectMapperTest {
	private static final Long RATING = 3L;
	private static final String REVIEW = "Good";
	private static final String SERIALISED_PUT_REQUEST = "{'rating':3,'date':9941,'review':'Good'}".replace("'", "\"");
	private static final Long DATE = 9941L;
	private JsonObjectMapper mapper;

	@BeforeEach
	public void setUp() {
		mapper = new JsonObjectMapper();
	}

	@Test
	public void testObjectIsSerialised() {
		// given
		Request request = new Request();
		request.setRating(RATING);
		request.setDate(DATE);
		request.setReview(REVIEW);

		// when
		String serialisedRequest = mapper.serialise(request);

		// then
		assertEquals(SERIALISED_PUT_REQUEST, serialisedRequest);
	}
	
	private static class Request {
		private Long rating;
		private Long date;
		private String review;

		public void setRating(Long rating) {
			this.rating = rating;
		}

		public void setDate(Long date) {
			this.date = date;
		}

		public void setReview(String review) {
			this.review = review;
		}
	}

}
