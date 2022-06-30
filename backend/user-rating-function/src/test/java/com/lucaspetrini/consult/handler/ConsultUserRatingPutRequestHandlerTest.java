package com.lucaspetrini.consult.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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

import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.response.PutUserRatingResponse;
import com.lucaspetrini.consult.service.UserRatingService;
import com.lucaspetrini.consult.service.model.UserRating;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Test {@link ConsultUserRatingPutRequestHandler}.
 *
 */
@ExtendWith(MockitoExtension.class)
public class ConsultUserRatingPutRequestHandlerTest {

	private static final String USER_ID_VALUE = "123";
	private static final String CODE_VALUE = "567";
	private static final UserRating USER_RATING;
	private static final Long RATING = 9L;
	private static final Long DATE = 5513564L;
	private static final String REVIEW = "Good value but it lacks potatoes";
	private static final Long VERSION = 3L;

	static {
		USER_RATING = new UserRating();
		USER_RATING.setSku(CODE_VALUE);
		USER_RATING.setUser(USER_ID_VALUE);
		USER_RATING.setRating(RATING);
		USER_RATING.setDate(DATE);
		USER_RATING.setReview(REVIEW);
		USER_RATING.setVersion(VERSION);
	}

	private ConsultUserRatingPutRequestHandler handler;
	private @Mock UserRatingService service;
	private @Captor ArgumentCaptor<String> userIdCaptor;
	private @Captor ArgumentCaptor<String> codeCaptor;
	private @Captor ArgumentCaptor<UserRating> userRatingCaptor;

	@BeforeEach
	public void setUp() {
		handler = new ConsultUserRatingPutRequestHandler();
		handler.setUserRatingService(service);
	}

	/*************************************************************
	 * \/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
	 *          ~~~~~~~~~~~ TESTS FOR PUT ~~~~~~~~~~~
	 * /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
	 ************************************************************/

	@Test
	public void testPutCallsUserRatingService() {
		// given
		HttpRequest<PutUserRatingRequest> request = new HttpRequest<>();
		Map<String, String> params = new HashMap<>();
		PutUserRatingRequest requestBody = new PutUserRatingRequest();
		requestBody.setRating(RATING);
		requestBody.setReview(REVIEW);
		request.setBody(requestBody);
		params.put(ConsultConstants.PATH_PARAM_USER_ID, USER_ID_VALUE);
		params.put(ConsultConstants.PATH_PARAM_CODE, CODE_VALUE);
		request.setPathParams(params);
		doReturn(USER_RATING).when(service).put(any());

		// when
		handler.handle(request);

		// then
		verify(service).put(userRatingCaptor.capture());
		UserRating rating = userRatingCaptor.getValue();
		assertEquals(USER_ID_VALUE, rating.getUser());
		assertEquals(CODE_VALUE, rating.getSku());
		assertTrue(Instant.ofEpochMilli(rating.getDate()).minus(1, ChronoUnit.SECONDS).isBefore(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
		assertTrue(Instant.ofEpochMilli(rating.getDate()).plus(1, ChronoUnit.SECONDS).isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
		assertEquals(RATING, rating.getRating());
		assertEquals(REVIEW, rating.getReview());
	}

	@Test
	public void testPutResponseReturnedMatchesEntityReturnedByUserRatingService() {
		// given
		HttpRequest<PutUserRatingRequest> request = new HttpRequest<>();
		request.setBody(new PutUserRatingRequest());
		request.setPathParams(Collections.emptyMap());
		doReturn(USER_RATING).when(service).put(any());

		// when
		HttpResponse<PutUserRatingResponse> response = handler.handle(request);

		// then
		PutUserRatingResponse responseBody = response.getBody();
		assertEquals(CODE_VALUE, responseBody.getCode());
		assertEquals(USER_ID_VALUE, responseBody.getUser());
		assertEquals(RATING, responseBody.getRating());
		assertEquals(DATE, responseBody.getDate());
		assertEquals(REVIEW, responseBody.getReview());
		assertEquals(VERSION, responseBody.getVersion());
	}

	@Test
	public void testPutResponseReturns200WhenEntityIsCreated() {
		// given
		HttpRequest<PutUserRatingRequest> request = new HttpRequest<>();
		request.setBody(new PutUserRatingRequest());
		request.setPathParams(Collections.emptyMap());
		doReturn(USER_RATING).when(service).put(any());

		// when
		HttpResponse<PutUserRatingResponse> response = handler.handle(request);

		// then
		assertEquals(200, response.getStatusCode());
	}

	@Test
	public void testPutResponseReturnsNoCustomHeadersWhenEntityIsCreated() {
		// given
		HttpRequest<PutUserRatingRequest> request = new HttpRequest<>();
		request.setBody(new PutUserRatingRequest());
		request.setPathParams(Collections.emptyMap());
		doReturn(USER_RATING).when(service).put(any());

		// when
		HttpResponse<PutUserRatingResponse> response = handler.handle(request);

		// then
		assertNull(response.getHeaders());
	}

	@Disabled("To be implemented later")
	@Test
	public void testPutDoesNotCallUserRatingServiceIfRequestIsNotValid() {
		fail("Not implemented.");
	}

	@Disabled("To be implemented later  (probably not, since it should be handled by Cognito)")
	@Test
	public void testPutDoesNotCallUserRatingServiceIfUserIdIsNotAuthenticated() {
		fail("Not implemented.");
	}

	@Disabled("To be implemented later")
	@Test
	public void testPutDoesNotCallUserRatingServiceIfUserIdIsNotAuthorised() {
		fail("Not implemented.");
	}

	@Disabled("To be implemented later  (probably not, since it should be handled by Cognito)")
	@Test
	public void testPutReturns401IfUserIdIsNotAuthenticated() {
		fail("Not implemented.");
	}

	@Disabled("To be implemented later")
	@Test
	public void testPutReturns403IfUserIdIsNotAuthorised() {
		fail("Not implemented.");
	}
}
