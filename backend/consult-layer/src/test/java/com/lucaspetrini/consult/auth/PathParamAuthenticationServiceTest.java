package com.lucaspetrini.consult.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Test {@link PathParamAuthenticationService}.
 */
public class PathParamAuthenticationServiceTest {
	private static final String USER = "Giuseppe";
	private static final String USER2 = "Maria";
	private PathParamAuthenticationService service;

	@BeforeEach
	public void setUp() {
		service = new PathParamAuthenticationService();
	}

	@Test
	public void testServiceReturnsTrueWhenJWTUserMatchesPathParam() {
		// given
		HttpRequest<?> request = new HttpRequest<>();
		request.setPathParams(Collections.singletonMap(ConsultConstants.PATH_PARAM_USER_ID, USER));
		request.setContext(Collections.singletonMap(ConsultConstants.CONTEXT_AUTHORIZATION_JWT_USER, USER));

		// when
		boolean result = service.isValid(request);

		// then
		assertTrue(result);
	}

	@Test
	public void testServiceReturnsFalseWhenJWTUserDoesNotMatchPathParam() {
		// given
		HttpRequest<?> request = new HttpRequest<>();
		request.setPathParams(Collections.singletonMap("user", USER));
		request.setContext(Collections.singletonMap(ConsultConstants.CONTEXT_AUTHORIZATION_JWT_USER, USER2));

		// when
		boolean result = service.isValid(request);

		// then
		assertFalse(result);
	}

	@Test
	public void testServiceReturnsFalseWhenJWTUserIsNull() {
		// given
		HttpRequest<?> request = new HttpRequest<>();
		request.setPathParams(Collections.singletonMap("user", USER));
		request.setContext(Collections.emptyMap());

		// when
		boolean result = service.isValid(request);

		// then
		assertFalse(result);
	}

	@Test
	public void testServiceReturnsFalseWhenPathParamIsNull() {
		// given
		HttpRequest<?> request = new HttpRequest<>();
		request.setPathParams(Collections.emptyMap());
		request.setContext(Collections.singletonMap(ConsultConstants.CONTEXT_AUTHORIZATION_JWT_USER, USER2));

		// when
		boolean result = service.isValid(request);

		// then
		assertFalse(result);
	}

}
