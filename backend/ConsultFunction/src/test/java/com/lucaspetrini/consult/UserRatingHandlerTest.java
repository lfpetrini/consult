package com.lucaspetrini.consult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.lucaspetrini.consult.handler.ConsultUserRatingRequestHandler;
import com.lucaspetrini.consult.mapper.ObjectMapper;
import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.response.PutUserRatingResponse;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Test {@link UserRatingHandler}.
 */
@ExtendWith(MockitoExtension.class)
public class UserRatingHandlerTest {

	private static final String ERROR_RESPONSE_DESERIALISER = "{\"errorDesc\":\"Cannot deserialise request body.\"}";
	private static final String ERROR_RESPONSE_INTERNAL = "{\"errorDesc\":\"Internal server error.\"}";
	private static final String VALID_PUT_RESPONSE_BODY = "{\"validPut\":\"Response.\"}";
	private static final String VALID_GET_RESPONSE_BODY = "{\"validGet\":\"Response.\"}";
	private static final PutUserRatingResponse VALID_PUT_RESPONSE = new PutUserRatingResponse();
	private static final GetUserRatingResponse VALID_GET_RESPONSE = new GetUserRatingResponse();
	private static final String ERROR_RESPONSE_UNSUPPORTED_POST = "{\"errorDesc\":\"Unsupported HTTP method POST.\"}";
	private static final String ERROR_RESPONSE_UNSUPPORTED_DELETE = "{\"errorDesc\":\"Unsupported HTTP method DELETE.\"}";
	private static final String ERROR_RESPONSE_UNSUPPORTED_APPLICATION_X_WWW_FORM_URLENCODED = "{\"errorDesc\":\"Unsupported Content-Type application/x-www-form-urlencoded.\"}";
	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String CUSTOM_HEADER = "cupcake";
	private static final String CUSTOM_HEADER_VALUE = "chocolate";

	private UserRatingHandler handler;
	private @Mock ObjectMapper objectMapper;
	private @Mock ConsultUserRatingRequestHandler requestHandler;
	private @Mock Context context;
	private @Captor ArgumentCaptor<HttpRequest<PutUserRatingRequest>> putUserRatingRequestCaptor;
	private @Captor ArgumentCaptor<HttpRequest<GetUserRatingRequest>> getUserRatingRequestCaptor;
	private APIGatewayProxyRequestEvent input;

	@BeforeEach
	public void setUp() {
		//System.setProperty("sqlite4java.library.path", "native-libs"); // DynamoDb only
		handler = new UserRatingHandler();
		handler.setObjectMapper(objectMapper);
		handler.setRequestHandler(requestHandler);
		input = new APIGatewayProxyRequestEvent();
	}

	@Test
	public void testRequestDeserialiserIsCalledForPutRequest() {
		// given
		input.setHttpMethod("PUT");

		// when
		handler.handleRequest(input, context);

		// then
		verify(objectMapper, times(1)).deserialise(input.getBody(), PutUserRatingRequest.class);
	}

	@Test
	public void testRequestDeserialiserIsCalledForGetRequest() {
		// given
		input.setHttpMethod("GET");

		// when
		handler.handleRequest(input, context);

		// then
		verify(objectMapper, times(1)).deserialise(input.getBody(), GetUserRatingRequest.class);
	}

	@Test
	public void testValidDeserialisedPutRequestIsPassedToRequestHandler() {
		// given
		input = createInput("PUT", null, "{}");
		PutUserRatingRequest putUserRating = new PutUserRatingRequest();
		doReturn(putUserRating).when(objectMapper).deserialise(any(String.class), ArgumentMatchers.<Class<PutUserRatingRequest>>any());

		// when
		handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(1)).handlePut(putUserRatingRequestCaptor.capture());
		assertEquals(putUserRating, putUserRatingRequestCaptor.getValue().getBody());
		assertEquals(input.getHeaders(), putUserRatingRequestCaptor.getValue().getHeaders());
	}

	@Test
	public void testValidDeserialisedGetRequestIsPassedToRequestHandler() {
		// given
		input = createInput("GET", null, "{}");
		GetUserRatingRequest getUserRating = new GetUserRatingRequest();
		doReturn(getUserRating).when(objectMapper).deserialise(any(String.class), ArgumentMatchers.<Class<GetUserRatingRequest>>any());

		// when
		handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(1)).handleGet(getUserRatingRequestCaptor.capture());
		assertEquals(getUserRating, getUserRatingRequestCaptor.getValue().getBody());
		assertEquals(input.getHeaders(), getUserRatingRequestCaptor.getValue().getHeaders());
	}

	@Test
	public void testResponseSerialiserIsCalledAfterRequestHandler_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<GetUserRatingResponse> response = new HttpResponse<>();
		response.setBody(VALID_GET_RESPONSE);
		doReturn(response).when(requestHandler).handleGet(any());

		// when
		handler.handleRequest(input, context);

		// then
		verify(objectMapper, times(1)).serialise(VALID_GET_RESPONSE);
	}

	@Test
	public void testResponseSerialiserIsCalledAfterRequestHandler_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		HttpResponse<PutUserRatingResponse> response = new HttpResponse<>();
		response.setBody(VALID_PUT_RESPONSE);
		doReturn(response).when(requestHandler).handlePut(any());

		// when
		handler.handleRequest(input, context);

		// then
		verify(objectMapper, times(1)).serialise(VALID_PUT_RESPONSE);
	}

	@Test
	public void testNullResponseReturnsServerError_ForGet() {
		// given
		input = createInput("GET", null, "{}");

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(500, (int)response.getStatusCode());
	}

	@Test
	public void testValidResponseIsReturned_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<GetUserRatingResponse> response = new HttpResponse<>();
		response.setBody(VALID_GET_RESPONSE);
		doReturn(response).when(requestHandler).handleGet(any());
		doReturn(VALID_GET_RESPONSE_BODY).when(objectMapper).serialise(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(VALID_GET_RESPONSE_BODY, responseEvent.getBody());
	}

	@Test
	public void testNullResponseReturnsServerError_ForPut() {
		// given
		input = createInput("PUT", null, "{}");

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(500, (int)response.getStatusCode());
	}

	@Test
	public void testValidResponseIsReturned_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		HttpResponse<PutUserRatingResponse> response = new HttpResponse<PutUserRatingResponse>();
		response.setBody(VALID_PUT_RESPONSE);
		doReturn(response).when(requestHandler).handlePut(any());
		doReturn(VALID_PUT_RESPONSE_BODY).when(objectMapper).serialise(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(VALID_PUT_RESPONSE_BODY, responseEvent.getBody());
	}

	@Test
	public void testValidDefaultResponseHeadersAreReturned_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<GetUserRatingRequest> response = new HttpResponse<>();
		response.setHeaders(null);
		response.setStatusCode(200);
		doReturn(response).when(requestHandler).handleGet(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(1, responseEvent.getHeaders().size());
		assertEquals(200, (int)responseEvent.getStatusCode());
	}

	@Test
	public void testValidDefaultResponseHeadersAreReturned_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		HttpResponse<PutUserRatingRequest> response = new HttpResponse<>();
		response.setHeaders(null);
		response.setStatusCode(201);
		doReturn(response).when(requestHandler).handlePut(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(1, responseEvent.getHeaders().size());
		assertEquals(201, (int)response.getStatusCode());
	}

	@Test
	public void testCustomResponseHeadersAreMergedWithDefault_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		HttpResponse<PutUserRatingRequest> response = new HttpResponse<>();
		response.setHeaders(Collections.singletonMap(CUSTOM_HEADER, CUSTOM_HEADER_VALUE));
		response.setStatusCode(201);
		doReturn(response).when(requestHandler).handlePut(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(CUSTOM_HEADER_VALUE, responseEvent.getHeaders().get(CUSTOM_HEADER));
		assertEquals(2, responseEvent.getHeaders().size());
		assertEquals(201, (int)response.getStatusCode());
	}

	@Disabled("Should this be allowed?")
	@Test
	public void testCustomResponseHeadersOverrideDefault_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		HttpResponse<PutUserRatingRequest> response = new HttpResponse<>();
		Map<String, String> headers = new HashMap<>();
		headers.put(CUSTOM_HEADER, CUSTOM_HEADER_VALUE);
		headers.put(ConsultConstants.HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);		response.setHeaders(headers);
		response.setStatusCode(201);
		doReturn(response).when(requestHandler).handlePut(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(APPLICATION_X_WWW_FORM_URLENCODED, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(CUSTOM_HEADER_VALUE, responseEvent.getHeaders().get(CUSTOM_HEADER));
		assertEquals(2, responseEvent.getHeaders().size());
		assertEquals(201, (int)response.getStatusCode());
	}

	@Test
	public void testCustomResponseHeadersAreMergedWithDefault_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<GetUserRatingRequest> response = new HttpResponse<>();
		response.setHeaders(Collections.singletonMap(CUSTOM_HEADER, CUSTOM_HEADER_VALUE));
		response.setStatusCode(201);
		doReturn(response).when(requestHandler).handleGet(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(CUSTOM_HEADER_VALUE, responseEvent.getHeaders().get(CUSTOM_HEADER));
		assertEquals(2, responseEvent.getHeaders().size());
		assertEquals(201, (int)response.getStatusCode());
	}

	@Disabled("Should this be allowed?")
	@Test
	public void testCustomResponseHeadersOverrideDefault_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<GetUserRatingRequest> response = new HttpResponse<>();
		Map<String, String> headers = new HashMap<>();
		headers.put(CUSTOM_HEADER, CUSTOM_HEADER_VALUE);
		headers.put(ConsultConstants.HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
		response.setHeaders(headers);
		response.setStatusCode(201);
		doReturn(response).when(requestHandler).handleGet(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		assertEquals(APPLICATION_X_WWW_FORM_URLENCODED, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(CUSTOM_HEADER_VALUE, responseEvent.getHeaders().get(CUSTOM_HEADER));
		assertEquals(2, responseEvent.getHeaders().size());
		assertEquals(201, (int)response.getStatusCode());
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
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(0)).handleGet(any());
		assertEquals(ERROR_RESPONSE_DESERIALISER, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, (int)response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnMapperDeserialiseException_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		doThrow(new RuntimeException()).when(objectMapper).deserialise(any(), any());

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(0)).handleGet(any());
		assertEquals(ERROR_RESPONSE_DESERIALISER, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, (int)response.getStatusCode());
	}
	@Test
	public void testErrorResponseIsReturnedOnMapperSerialiseException_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		HttpResponse<GetUserRatingResponse> response = new HttpResponse<>();
		response.setBody(VALID_GET_RESPONSE);
		doReturn(response).when(requestHandler).handleGet(any());
		doThrow(new RuntimeException()).when(objectMapper).serialise(any());

		// when
		APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(1)).handleGet(any());
		assertEquals(ERROR_RESPONSE_INTERNAL, responseEvent.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, responseEvent.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(500, (int)responseEvent.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnMapperSerialiseException_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		doReturn(new HttpResponse<PutUserRatingResponse>()).when(requestHandler).handlePut(any());
		doThrow(new RuntimeException()).when(objectMapper).serialise(any());

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(1)).handlePut(any());
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(500, (int)response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnRequestHandlerException_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		doThrow(new RuntimeException()).when(requestHandler).handlePut(any());

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(500, (int)response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnRequestHandlerException_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		doThrow(new RuntimeException()).when(requestHandler).handleGet(any());

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(500, (int)response.getStatusCode());
	}

	/**
	 * TODO decouple?
	 */
	@Test
	public void testErrorResponseIsReturnedOnInvalidContentType() {
		// given
		input = createInput("PUT", Collections.singletonMap("Content-Type", APPLICATION_X_WWW_FORM_URLENCODED), "{}");

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verifyNoInteractions(requestHandler);
		assertEquals(ERROR_RESPONSE_UNSUPPORTED_APPLICATION_X_WWW_FORM_URLENCODED, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, (int)response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnInvalidMethod_POST() {
		// given
		input = createInput("POST", null, "{}");

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verifyNoInteractions(requestHandler);
		assertEquals(ERROR_RESPONSE_UNSUPPORTED_POST, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, (int)response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnInvalidMethod_DELETE() {
		// given
		input = createInput("DELETE", null, "{}");

		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verifyNoInteractions(requestHandler);
		assertEquals(ERROR_RESPONSE_UNSUPPORTED_DELETE, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, (int)response.getStatusCode());
	}

	private APIGatewayProxyRequestEvent createInput(String method, Map<String, String> headers, String body) {
		APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
		event.setHttpMethod(method);
		event.setHeaders(headers);
		event.setBody(body);
		return event;
	}
}
