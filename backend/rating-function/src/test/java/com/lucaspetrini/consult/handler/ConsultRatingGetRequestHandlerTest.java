package com.lucaspetrini.consult.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucaspetrini.consult.exception.ResourceNotFoundException;
import com.lucaspetrini.consult.request.GetRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.GetRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.service.RatingService;
import com.lucaspetrini.consult.service.model.Rating;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Test {@link ConsultRatingGetRequestHandler}.
 *
 */
@ExtendWith(MockitoExtension.class)
public class ConsultRatingGetRequestHandlerTest {

	private static final Long QUANTITY = 11L;
	private static final String CODE_VALUE = "567";
	private static final Rating RATING;
	private static final Long AGGREGATED = 19L;
	private static final Long DATE = 5513564L;
	private static final Long VERSION = 3L;

	static {
		RATING = new Rating();
		RATING.setSku(CODE_VALUE);
		RATING.setQuantity(QUANTITY);
		RATING.setAggregated(AGGREGATED);
		RATING.setDate(DATE);
		RATING.setVersion(VERSION);
	}

	private ConsultRatingGetRequestHandler handler;
	private @Mock RatingService service;
	private @Captor ArgumentCaptor<String> codeCaptor;
	private @Captor ArgumentCaptor<Rating> ratingCaptor;

	@BeforeEach
	public void setUp() {
		handler = new ConsultRatingGetRequestHandler();
		handler.setRatingService(service);
	}

	/*************************************************************
	 * \/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
	 *          ~~~~~~~~~~~ TESTS FOR GET ~~~~~~~~~~~
	 * /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
	 ************************************************************/

	@Test
	public void testGetBySkuCallsRatingService() {
		// given
		HttpRequest<GetRatingRequest> request = new HttpRequest<>();
		Map<String, String> params = new HashMap<>();
		params.put(ConsultConstants.PATH_PARAM_CODE, CODE_VALUE);
		request.setPathParams(params);
		doReturn(RATING).when(service).getByCode(CODE_VALUE);

		// when
		handler.handle(request);

		// then
		verify(service).getByCode(codeCaptor.capture());
		assertEquals(CODE_VALUE, codeCaptor.getValue());
	}

	@Disabled("To be implemented later")
	@Test
	public void testGetWithMissingPathParamsThrowsInvalidPathParameterException() {
		fail("Not implemented.");
	}

	@Test
	public void testGetResponseReturnedMatchesEntityReturnedByRatingService() {
		// given
		HttpRequest<GetRatingRequest> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		doReturn(RATING).when(service).getByCode(any());

		// when
		HttpResponse<GetRatingResponse> response = handler.handle(request);

		// then
		GetRatingResponse responseBody = response.getBody();
		assertEquals(CODE_VALUE, responseBody.getCode());
		assertEquals(QUANTITY, responseBody.getQuantity());
		assertEquals(AGGREGATED, responseBody.getAggregated());
		assertEquals(DATE, responseBody.getDate());
	}

	@Test
	public void testGetResponseReturns200WhenEntityIsFound() {
		// given
		HttpRequest<GetRatingRequest> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		doReturn(RATING).when(service).getByCode(any());

		// when
		HttpResponse<GetRatingResponse> response = handler.handle(request);

		// then
		assertEquals(200, response.getStatusCode());
	}

	@Test
	public void testGetResponseReturnsNoCustomHeadersWhenEntityIsFound() {
		// given
		HttpRequest<GetRatingRequest> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		doReturn(RATING).when(service).getByCode(any());

		// when
		HttpResponse<GetRatingResponse> response = handler.handle(request);

		// then
		assertNull(response.getHeaders());
	}

	@Test
	public void testGetResponseThrowsResourceNotFoundExceptionWhenEntityIsNotFound() {
		// given
		HttpRequest<GetRatingRequest> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		doReturn(null).when(service).getByCode(any());

		// then
		assertThrows(ResourceNotFoundException.class, () -> { 
			// when
			handler.handle(request);
		});
	}
}
