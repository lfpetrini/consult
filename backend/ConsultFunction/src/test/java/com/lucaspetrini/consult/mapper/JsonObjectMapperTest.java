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
	private static final String SKU = "SKU123";
	private static final String USER = "mrpickles";
	private static final Integer RATING = 3;
	private static final String SERIALISED_PUT_REQUEST = "{'sku':'SKU123','user':'mrpickles','rating':3}".replace("'", "\"");
	private static final String DESERIALISED_GET_RESPONSE = "{'sku':'SKU123','user':'mrpickles','rating':3}".replace("'", "\"");
	private JsonObjectMapper mapper;

	@BeforeEach
	public void setUp() {
		mapper = new JsonObjectMapper();
	}

	@Test
	public void testPutRequestIsSerialised() {
		PutUserRatingRequest request = new PutUserRatingRequest();
		request.setSku(SKU);
		request.setUser(USER);
		request.setRating(RATING);
		
		String serialisedRequest = mapper.serialise(request);
		
		assertEquals(SERIALISED_PUT_REQUEST, serialisedRequest);
	}

	@Test
	public void testGetResponseIsDeserialised() {
		GetUserRatingResponse response = mapper.deserialise(DESERIALISED_GET_RESPONSE, GetUserRatingResponse.class);

		assertEquals(SKU, response.getSku());
		assertEquals(USER, response.getUser());
		assertEquals(RATING, response.getRating());
	}

}
