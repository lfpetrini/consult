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

import com.lucaspetrini.consult.dao.UserRatingDao;
import com.lucaspetrini.consult.exception.ResourceNotFoundException;
import com.lucaspetrini.consult.model.UserRating;
import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Test {@link DefaultConsultRequestHandler}.
 *
 */
@ExtendWith(MockitoExtension.class)
public class DefaultConsultRequestHandlerTest {
	
	private static final String USER_ID_VALUE = "123";
	private static final String CODE_VALUE = "567";
	private static final UserRating USER_RATING;
	private static final Integer RATING = 9;
	private static final Long DATE = 5513564L;
	private static final String REVIEW = "Good value but it lacks potatoes";
	
	static {
		USER_RATING = new UserRating();
		USER_RATING.setSku(CODE_VALUE);
		USER_RATING.setUser(USER_ID_VALUE);
		USER_RATING.setRating(RATING);
		USER_RATING.setDate(DATE);
		USER_RATING.setReview(REVIEW);
	}

	private DefaultConsultRequestHandler handler;
	private @Mock UserRatingDao dao;
	private @Captor ArgumentCaptor<String> userIdCaptor;
	private @Captor ArgumentCaptor<String> codeCaptor;
	
	@BeforeEach
	public void setUp() {
		handler = new DefaultConsultRequestHandler();
		handler.setUserRatingDao(dao);
	}

	@Test
	public void testGetBySkuAndUserCallDAOObject() {
		// given
		HttpRequest<GetUserRatingRequest> request = new HttpRequest<>();
		Map<String, String> params = new HashMap<>();
		params.put(ConsultConstants.PATH_PARAM_USER_ID, USER_ID_VALUE);
		params.put(ConsultConstants.PATH_PARAM_CODE, CODE_VALUE);
		request.setPathParams(params);
		doReturn(USER_RATING).when(dao).getByUserIdAndCode(any(), any());
		
		// when
		handler.handleGet(request);
		
		// then
		verify(dao).getByUserIdAndCode(userIdCaptor.capture(), codeCaptor.capture());
		assertEquals(USER_ID_VALUE, userIdCaptor.getValue());
		assertEquals(CODE_VALUE, codeCaptor.getValue());
	}

	@Disabled("To be implemented later")
	@Test
	public void testGetAllByUserCallDAOObjectWithoutLimitsWhenLimitIsNotProvided() {
		fail("Not implemented.");
	}

	@Disabled("To be implemented later")
	@Test
	public void testGetAllByUserCallDAOObjectWithLimitWhenLimitIsProvided() {
		fail("Not implemented.");
	}

	@Disabled("To be implemented later")
	@Test
	public void testGetWithMissingPathParamsThrowsInvalidPathParameterException() {
		fail("Not implemented.");
	}

	@Test
	public void testGetResponseReturnedMatchesEntityReturnedByDAO() {
		// given
		HttpRequest<GetUserRatingRequest> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		doReturn(USER_RATING).when(dao).getByUserIdAndCode(any(), any());
		
		// when
		HttpResponse<GetUserRatingResponse> response = handler.handleGet(request);
		
		// then
		GetUserRatingResponse responseBody = response.getBody();
		assertEquals(CODE_VALUE, responseBody.getSku());
		assertEquals(USER_ID_VALUE, responseBody.getUser());
		assertEquals(RATING, responseBody.getRating());
		assertEquals(DATE, responseBody.getDate());
		assertEquals(REVIEW, responseBody.getReview());
	}

	@Test
	public void testGetResponseReturns200WhenEntityIsFound() {
		// given
		HttpRequest<GetUserRatingRequest> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		doReturn(USER_RATING).when(dao).getByUserIdAndCode(any(), any());
		
		// when
		HttpResponse<GetUserRatingResponse> response = handler.handleGet(request);
		
		// then
		assertEquals(200, response.getStatusCode());
	}

	@Test
	public void testGetResponseReturnsNoCustomHeadersWhenEntityIsFound() {
		// given
		HttpRequest<GetUserRatingRequest> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		doReturn(USER_RATING).when(dao).getByUserIdAndCode(any(), any());
		
		// when
		HttpResponse<GetUserRatingResponse> response = handler.handleGet(request);
		
		// then
		assertNull(response.getHeaders());
	}

	@Test
	public void testGetResponseThrowsResourceNotFoundExceptionWhenEntityIsNotFound() {
		// given
		// given
		HttpRequest<GetUserRatingRequest> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		doReturn(null).when(dao).getByUserIdAndCode(any(), any());
		
		// then
		assertThrows(ResourceNotFoundException.class, () -> { 
			// when
			handler.handleGet(request);
		});
	}

}
