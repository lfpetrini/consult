package com.lucaspetrini.consult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Authorizer;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Http;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.lucaspetrini.consult.auth.AuthenticationService;
import com.lucaspetrini.consult.handler.ConsultRequestHandler;
import com.lucaspetrini.consult.mapper.ObjectMapper;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Test {@link AbstractRequestHandler}.
 */
@ExtendWith(MockitoExtension.class)
public class AbstractRequestHandlerTest {
	public static class TestResponse {}
	public static class TestRequest {}

	private static final String ERROR_RESPONSE_DESERIALISER = "{\"errorDesc\":\"Cannot deserialise request body.\"}";
	private static final String ERROR_RESPONSE_INTERNAL = "{\"errorDesc\":\"Internal server error.\"}";
	private static final String VALID_GET_RESPONSE_BODY = "{\"validGet\":\"Response.\"}";
	private static final TestResponse VALID_GET_RESPONSE = new TestResponse();
	private static final String ERROR_RESPONSE_UNSUPPORTED_POST = "{\"errorDesc\":\"Unsupported HTTP method POST.\"}";
	private static final String ERROR_RESPONSE_UNSUPPORTED_PUT = "{\"errorDesc\":\"Unsupported HTTP method PUT.\"}";
	private static final String ERROR_RESPONSE_UNSUPPORTED_DELETE = "{\"errorDesc\":\"Unsupported HTTP method DELETE.\"}";
	private static final Object ERROR_RESPONSE_NOT_ALLOWED = "{\"errorDesc\":\"Not allowed.\"}";
	private static final String CUSTOM_HEADER = "cupcake";
	private static final String CUSTOM_HEADER_VALUE = "chocolate";
	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String JWT_USERNAME = "n00bPwn3r";

	private AbstractRequestHandler handler;
	private @Mock ObjectMapper objectMapper;
	private @Mock ConsultRequestHandler<TestRequest, TestResponse> requestHandlerGet;
	private @Mock Context context;
	private @Captor ArgumentCaptor<HttpRequest<TestRequest>> testRequestCaptor;
	private APIGatewayV2HTTPEvent input;

	@BeforeEach
	public void setUp() {
		handler = new AbstractRequestHandler() { };
		handler.setObjectMapper(objectMapper);
		handler.addRequestHandlerMap(HttpMethod.GET, requestHandlerGet, TestRequest.class);
		input = new APIGatewayV2HTTPEvent();
	}

	@Test
	public void testRequestDeserialiserIsCalledForGetRequest() {
		// given
		input = createInput("GET", null, "{}");

		// when
		handler.handleRequest(input, context);

		// then
		verify(objectMapper, times(1)).deserialise(input.getBody(), TestRequest.class);
	}

	@Test
	public void testValidDeserialisedGetRequestIsPassedTorequestHandlerGet() {
		// given
		Map<String, String> pathParams = Collections.singletonMap("param1", "value1");
		input = createInput("GET", null, "{}", pathParams);
		TestRequest testRequest = new TestRequest();
		doReturn(testRequest).when(objectMapper).deserialise(any(String.class), ArgumentMatchers.<Class<TestRequest>>any());

		// when
		handler.handleRequest(input, context);

		// then
		verify(requestHandlerGet, times(1)).handle(testRequestCaptor.capture());
		assertEquals(testRequest, testRequestCaptor.getValue().getBody());
		assertEquals(input.getHeaders(), testRequestCaptor.getValue().getHeaders());
		assertEquals(input.getPathParameters(), testRequestCaptor.getValue().getPathParams());
	}

	@Test
	public void testResponseSerialiserIsCalledAfterrequestHandlerGet_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<TestResponse> response = new HttpResponse<>();
		response.setBody(VALID_GET_RESPONSE);
		doReturn(response).when(requestHandlerGet).handle(any());

		// when
		handler.handleRequest(input, context);

		// then
		verify(objectMapper, times(1)).serialise(VALID_GET_RESPONSE);
	}

	@Test
	public void testNullResponseReturnsServerError_ForGet() {
		// given
		input = createInput("GET", null, "{}");

		// when
		APIGatewayV2HTTPResponse response = handler.handleRequest(input, context);

		// then
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(500, response.getStatusCode());
	}

	@Test
	public void testValidResponseIsReturned_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<TestResponse> response = new HttpResponse<>();
		response.setBody(VALID_GET_RESPONSE);
		doReturn(response).when(requestHandlerGet).handle(any());
		doReturn(VALID_GET_RESPONSE_BODY).when(objectMapper).serialise(any());

		// when
		APIGatewayV2HTTPResponse responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(VALID_GET_RESPONSE_BODY, responseEvent.getBody());
	}

	@Test
	public void testValidDefaultResponseHeadersAreReturned_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<TestResponse> response = new HttpResponse<>();
		response.setHeaders(null);
		response.setStatusCode(200);
		doReturn(response).when(requestHandlerGet).handle(any());

		// when
		APIGatewayV2HTTPResponse responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(1, responseEvent.getHeaders().size());
		assertEquals(200, responseEvent.getStatusCode());
	}

	@Test
	public void testCustomResponseHeadersAreMergedWithDefault_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<TestResponse> response = new HttpResponse<>();
		response.setHeaders(Collections.singletonMap(CUSTOM_HEADER, CUSTOM_HEADER_VALUE));
		response.setStatusCode(201);
		doReturn(response).when(requestHandlerGet).handle(any());

		// when
		APIGatewayV2HTTPResponse responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(CUSTOM_HEADER_VALUE, responseEvent.getHeaders().get(CUSTOM_HEADER));
		assertEquals(2, responseEvent.getHeaders().size());
		assertEquals(201, response.getStatusCode());
	}

	@Disabled("Should this be allowed?")
	@Test
	public void testCustomResponseHeadersOverrideDefault_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<TestResponse> response = new HttpResponse<>();
		Map<String, String> headers = new HashMap<>();
		headers.put(CUSTOM_HEADER, CUSTOM_HEADER_VALUE);
		headers.put(ConsultConstants.HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
		response.setHeaders(headers);
		response.setStatusCode(201);
		doReturn(response).when(requestHandlerGet).handle(any());

		// when
		APIGatewayV2HTTPResponse responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(APPLICATION_X_WWW_FORM_URLENCODED, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(CUSTOM_HEADER_VALUE, responseEvent.getHeaders().get(CUSTOM_HEADER));
		assertEquals(2, responseEvent.getHeaders().size());
		assertEquals(201, response.getStatusCode());
	}

	/**
	 * TODO decouple exception handling -> create a handler to convert known exceptions to API error codes
	 */
	@Test
	public void testErrorResponseIsReturnedOnMapperDeserialiseException_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		doThrow(new RuntimeException()).when(objectMapper).deserialise(any(), any());

		// when
		APIGatewayV2HTTPResponse response = handler.handleRequest(input, context);

		// then
		verify(requestHandlerGet, times(0)).handle(any());
		assertEquals(ERROR_RESPONSE_DESERIALISER, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnMapperSerialiseException_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<TestResponse> response = new HttpResponse<>();
		response.setBody(VALID_GET_RESPONSE);
		doReturn(response).when(requestHandlerGet).handle(any());
		doThrow(new RuntimeException()).when(objectMapper).serialise(any());

		// when
		APIGatewayV2HTTPResponse responseEvent = handler.handleRequest(input, context);

		// then
		verify(requestHandlerGet, times(1)).handle(any());
		assertEquals(ERROR_RESPONSE_INTERNAL, responseEvent.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(500, responseEvent.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnrequestHandlerGetException_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		doThrow(new RuntimeException()).when(requestHandlerGet).handle(any());

		// when
		APIGatewayV2HTTPResponse response = handler.handleRequest(input, context);

		// then
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(500, response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnInvalidMethod_POST() {
		// given
		input = createInput("POST", null, "{}");

		// when
		APIGatewayV2HTTPResponse response = handler.handleRequest(input, context);

		// then
		verifyNoInteractions(requestHandlerGet);
		assertEquals(ERROR_RESPONSE_UNSUPPORTED_POST, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnInvalidMethod_PUT() {
		// given
		input = createInput("PUT", null, "{}");

		// when
		APIGatewayV2HTTPResponse response = handler.handleRequest(input, context);

		// then
		verifyNoInteractions(requestHandlerGet);
		assertEquals(ERROR_RESPONSE_UNSUPPORTED_PUT, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnInvalidMethod_DELETE() {
		// given
		input = createInput("DELETE", null, "{}");

		// when
		APIGatewayV2HTTPResponse response = handler.handleRequest(input, context);

		// then
		verifyNoInteractions(requestHandlerGet);
		assertEquals(ERROR_RESPONSE_UNSUPPORTED_DELETE, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, response.getStatusCode());
	}

	@Test
	public void testAuthenticationServiceIsCalledBeforeRequestHandler() {
		// given
		input = createInput("GET", null, "{}");
		AuthenticationService authService = mock(AuthenticationService.class);
		doReturn(true).when(authService).isValid(any(HttpRequest.class));
		handler.addRequestHandlerMap(HttpMethod.GET, requestHandlerGet, TestRequest.class, authService);

		// when
		handler.handleRequest(input, context);

		// then
		InOrder inOrder = inOrder(authService, requestHandlerGet);
		inOrder.verify(authService).isValid(any(HttpRequest.class));
		inOrder.verify(requestHandlerGet).handle(Mockito.<HttpRequest<TestRequest>>any());
	}

	@Test
	public void testRequestHandlerIsNotCalledIfAuthenticationServiceReturnsFalse() {
		// given
		input = createInput("GET", null, "{}");
		AuthenticationService authService = mock(AuthenticationService.class);
		doReturn(false).when(authService).isValid(any(HttpRequest.class));
		handler.addRequestHandlerMap(HttpMethod.GET, requestHandlerGet, TestRequest.class, authService);

		// when
		handler.handleRequest(input, context);

		// then
		verifyNoInteractions(requestHandlerGet);
	}

	@Test
	public void test403ResponseIsReturnedIffAuthenticationServiceReturnsFalse() {
		// given
		input = createInput("GET", null, "{}");
		AuthenticationService authService = mock(AuthenticationService.class);
		doReturn(false).when(authService).isValid(any(HttpRequest.class));
		handler.addRequestHandlerMap(HttpMethod.GET, requestHandlerGet, TestRequest.class, authService);

		// when
		APIGatewayV2HTTPResponse response = handler.handleRequest(input, context);

		// then
		verifyNoInteractions(requestHandlerGet);
		assertEquals(ERROR_RESPONSE_NOT_ALLOWED, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(403, response.getStatusCode());
	}

	@Test
	public void testUsernameIsAddedToContextWhenDeserialisedGetRequestIsPassedTorequestHandlerGet() {
		// given
		input = createInput("GET", null, "{}");
		input.getRequestContext().setAuthorizer(Authorizer.builder().withJwt(
				JWT.builder().withClaims(Collections.singletonMap("username", JWT_USERNAME)).build()
				).build());
		TestRequest testRequest = new TestRequest();
		doReturn(testRequest).when(objectMapper).deserialise(any(String.class), ArgumentMatchers.<Class<TestRequest>>any());

		// when
		handler.handleRequest(input, context);

		// then
		verify(requestHandlerGet, times(1)).handle(testRequestCaptor.capture());
		assertEquals(JWT_USERNAME, testRequestCaptor.getValue().getContext().get(ConsultConstants.CONTEXT_AUTHORIZATION_JWT_USER));
	}

	private APIGatewayV2HTTPEvent createInput(String method, Map<String, String> headers, String body) {
		return createInput(method, headers, body, null);
	}

	private APIGatewayV2HTTPEvent createInput(String method, Map<String, String> headers, String body, Map<String, String> pathParams) {
		RequestContext reqContext = new RequestContext();
		Http http = new Http();
		http.setMethod(method);
		reqContext.setHttp(http);
		APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
		event.setRequestContext(reqContext);
		event.setHeaders(headers);
		event.setBody(body);
		event.setPathParameters(pathParams);
		return event;
	}
}
