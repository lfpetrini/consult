package com.lucaspetrini.consult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.lucaspetrini.consult.handler.ConsultRequestHandler;
import com.lucaspetrini.consult.mapper.JsonRequestMapper;
import com.lucaspetrini.consult.request.GetUserRatingsRequest;
import com.lucaspetrini.consult.request.PutUserRatingsRequest;
import com.lucaspetrini.consult.response.GetUserRatingsResponse;
import com.lucaspetrini.consult.response.PutUserRatingsResponse;
import com.lucaspetrini.consult.utils.ConsultConstants;

@RunWith(MockitoJUnitRunner.class)
public class UserRatingsHandlerTest {

	private static final String ERROR_RESPONSE_DESERIALISER = "{\"errorDesc\":\"Cannot deserialise request body.\"}";
	private static final String ERROR_RESPONSE_INTERNAL = "{\"errorDesc\":\"Internal server error.\"}";
	private static final String VALID_PUT_RESPONSE_BODY = "{\"validPut\":\"Response.\"}";
	private static final String VALID_GET_RESPONSE_BODY = "{\"validGet\":\"Response.\"}";
	private static final PutUserRatingsResponse VALID_PUT_RESPONSE = new PutUserRatingsResponse();
	private static final GetUserRatingsResponse VALID_GET_RESPONSE = new GetUserRatingsResponse();
	private static final String ERROR_RESPONSE_UNSUPPORTED_POST = "{\"errorDesc\":\"Unsupported HTTP method POST.\"}";
	private static final String ERROR_RESPONSE_UNSUPPORTED_APPLICATION_X_WWW_FORM_URLENCODED = "{\"errorDesc\":\"Unsupported Content-Type application/x-www-form-urlencoded.\"}";
	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

	private UserRatingsHandler handler;
	private @Mock JsonRequestMapper requestMapper;
	private @Mock ConsultRequestHandler requestHandler;
	private @Mock Context context;
	private APIGatewayProxyRequestEvent input;
	
	@Before
	public void setUp() {
		//System.setProperty("sqlite4java.library.path", "native-libs"); // DynamoDb only
		handler = new UserRatingsHandler();
		handler.setRequestMapper(requestMapper);
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
		verify(requestMapper, times(1)).deserialise(input.getBody(), PutUserRatingsRequest.class);
	}

	@Test
	public void testRequestDeserialiserIsCalledForGetRequest() {
		// given
		input.setHttpMethod("GET");

		// when
		handler.handleRequest(input, context);

		// then
		verify(requestMapper, times(1)).deserialise(input.getBody(), GetUserRatingsRequest.class);
	}

	@Test
	public void testValidDeserialisedPutRequestIsPassedToRequestHandler() {
		// given
		input = createInput("PUT", null, "{}");
		PutUserRatingsRequest putUserRatings = new PutUserRatingsRequest();
		doReturn(putUserRatings).when(requestMapper).deserialise(any(String.class), ArgumentMatchers.<Class<PutUserRatingsRequest>>any());
		
		// when
		handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(1)).handlePut(input.getHeaders(), putUserRatings);
	}

	@Test
	public void testValidDeserialisedGetRequestIsPassedToRequestHandler() {
		// given
		input = createInput("GET", null, "{}");
		GetUserRatingsRequest getUserRatings = new GetUserRatingsRequest();
		doReturn(getUserRatings).when(requestMapper).deserialise(any(String.class), ArgumentMatchers.<Class<GetUserRatingsRequest>>any());
		
		// when
		handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(1)).handleGet(input.getHeaders(), getUserRatings);
	}

	@Test
	public void testResponseSerialiserIsCalledAfterRequestHandler_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		doReturn(VALID_GET_RESPONSE).when(requestHandler).handleGet(any(), any());
		
		// when
		handler.handleRequest(input, context);

		// then
		verify(requestMapper, times(1)).serialise(VALID_GET_RESPONSE);
	}

	@Test
	public void testResponseSerialiserIsCalledAfterRequestHandler_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		/*PutUserRatingsRequest putUserRatings = new PutUserRatingsRequest();
		doReturn(putUserRatings).when(requestMapper).deserialise(any(String.class), ArgumentMatchers.<Class<PutUserRatingsRequest>>any());
		doReturn(VALID_PUT_RESPONSE).when(requestHandler).handlePut(input.getHeaders(), putUserRatings);*/
		doReturn(VALID_PUT_RESPONSE).when(requestHandler).handlePut(any(), any());
		
		// when
		handler.handleRequest(input, context);

		// then
		verify(requestMapper, times(1)).serialise(VALID_PUT_RESPONSE);
	}

	@Test
	public void testValidResponseIsReturned_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		doReturn(VALID_GET_RESPONSE_BODY).when(requestMapper).serialise(any());
		
		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		assertEquals(VALID_GET_RESPONSE_BODY, response.getBody());
	}

	@Test
	public void testValidResponseIsReturned_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		doReturn(VALID_PUT_RESPONSE_BODY).when(requestMapper).serialise(any());
		
		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		assertEquals(VALID_PUT_RESPONSE_BODY, response.getBody());
	}

	@Test
	public void testValidResponseHeadersAreReturned_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		GetUserRatingsRequest getUserRatings = new GetUserRatingsRequest();
		doReturn(getUserRatings).when(requestMapper).deserialise(any(String.class), ArgumentMatchers.<Class<GetUserRatingsRequest>>any());
		
		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(200, (int)response.getStatusCode());
	}

	@Test
	public void testValidResponseHeadersAreReturned_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		PutUserRatingsRequest puttUserRatings = new PutUserRatingsRequest();
		doReturn(puttUserRatings).when(requestMapper).deserialise(any(String.class), ArgumentMatchers.<Class<PutUserRatingsRequest>>any());
		
		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(201, (int)response.getStatusCode());
	}

	/**
	 * TODO decouple exception handling -> create a handler to convert known exceptions to API error codes
	 */
	@Test
	public void testErrorResponseIsReturnedOnMapperDeserialiseException_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		doThrow(new RuntimeException()).when(requestMapper).deserialise(any(), any());
		
		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(0)).handleGet(any(), any());
		assertEquals(ERROR_RESPONSE_DESERIALISER, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, (int)response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnMapperDeserialiseException_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		doThrow(new RuntimeException()).when(requestMapper).deserialise(any(), any());
		
		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(0)).handleGet(any(), any());
		assertEquals(ERROR_RESPONSE_DESERIALISER, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(400, (int)response.getStatusCode());
	}
	@Test
	public void testErrorResponseIsReturnedOnMapperSerialiseException_ForGet() {
		// given
		input = createInput("GET", null, "{}");
		doThrow(new RuntimeException()).when(requestMapper).serialise(any());
		
		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(1)).handleGet(any(), any());
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(500, (int)response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnMapperSerialiseException_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		doThrow(new RuntimeException()).when(requestMapper).serialise(any());
		
		// when
		APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

		// then
		verify(requestHandler, times(1)).handlePut(any(), any());
		assertEquals(ERROR_RESPONSE_INTERNAL, response.getBody());
		assertEquals(ConsultConstants.CONTENT_TYPE_JSON, response.getHeaders().get(ConsultConstants.HEADER_CONTENT_TYPE));
		assertEquals(500, (int)response.getStatusCode());
	}

	@Test
	public void testErrorResponseIsReturnedOnRequestHandlerException_ForPut() {
		// given
		input = createInput("PUT", null, "{}");
		doThrow(new RuntimeException()).when(requestHandler).handlePut(any(), any());
		
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
		doThrow(new RuntimeException()).when(requestHandler).handleGet(any(), any());
		
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
	public void testErrorResponseIsReturnedOnInvalidMethod() {
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


	private APIGatewayProxyRequestEvent createInput(String method, Map<String, String> headers, String body) {
		APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
		event.setHttpMethod(method);
		event.setHeaders(headers);
		event.setBody(body);
		return event;
	}
}
