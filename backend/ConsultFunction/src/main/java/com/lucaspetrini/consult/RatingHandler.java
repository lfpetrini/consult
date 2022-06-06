package com.lucaspetrini.consult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.lucaspetrini.consult.exception.RequestDeserialisationException;
import com.lucaspetrini.consult.exception.ServiceException;
import com.lucaspetrini.consult.exception.UnsupportedContentTypeException;
import com.lucaspetrini.consult.exception.UnsupportedMethodException;
import com.lucaspetrini.consult.handler.ConsultRatingRequestHandler;
import com.lucaspetrini.consult.mapper.ObjectMapper;
import com.lucaspetrini.consult.request.GetRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.GetRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Handler for requests to Lambda function.
 */
public class RatingHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private ObjectMapper objectMapper;
	private ConsultRatingRequestHandler requestHandler;

	/**
	 * Main request handler.
	 * 
	 * @param input APIGatewayProxyRequest event.
	 * @param context Lambda execution environment context.
	 */
	@Override
	public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
		HttpResponse<?>  response = null;
		String responseBody = null;
		try {
			// TODO: refactor
			validateHeaders(input.getHeaders());
			String method = input.getHttpMethod();
			switch (method) {
			case ConsultConstants.HTTP_METHOD_GET:
				response = handleGet(input, context);
				break;
			default:
				UnsupportedMethodException invalidMethodException = new UnsupportedMethodException(method);
				throw invalidMethodException ; // change to a different ServiceException
			}
			responseBody = objectMapper.serialise(response.getBody());
		}
		catch (Exception e) {
			return buildErrorResponse(e);
		}
		Map<String, String> responseHeaders = new HashMap<>();
		responseHeaders.put(ConsultConstants.HEADER_CONTENT_TYPE, ConsultConstants.CONTENT_TYPE_JSON);
		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
		if(response != null) {
			if(response.getHeaders() != null) {
				responseHeaders.putAll(response.getHeaders());
			}
			if(response.getBody() != null) {
				responseEvent.setBody(responseBody);
			}
			responseEvent.setStatusCode(response.getStatusCode());
		}
		responseEvent.setHeaders(responseHeaders);
		responseEvent.setStatusCode(200);
		return responseEvent;
	}

	// TODO refactor
	private void validateHeaders(Map<String, String> headers) {
		if(headers == null)
			return;
		// get case insensitive content-type or defaults to application/json if not found
		String contentType = headers.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(ConsultConstants.HEADER_CONTENT_TYPE)).map(e -> e.getValue()).findFirst().orElse(ConsultConstants.CONTENT_TYPE_JSON);
		if(!ConsultConstants.CONTENT_TYPE_JSON.equals(contentType)) {
			throw new UnsupportedContentTypeException(contentType);
		}
	}

	// TODO refactor
	private APIGatewayProxyResponseEvent buildErrorResponse(Exception e) {
		String errorMessage = ConsultConstants.UNHANDLED_EXCEPTION_ERROR_DESC;
		int statusCode = 500;
		if(e instanceof ServiceException) {
			errorMessage = ((ServiceException)e).getShortDescription();
			statusCode = ((ServiceException)e).getStatusCode();
		}
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setBody("{\"errorDesc\":\"" + errorMessage + "\"}");
		response.setStatusCode(statusCode);
		response.setHeaders(Collections.singletonMap(ConsultConstants.HEADER_CONTENT_TYPE, ConsultConstants.CONTENT_TYPE_JSON));
		return response;
	}

	// TODO refactor
	private HttpResponse<GetRatingResponse> handleGet(APIGatewayProxyRequestEvent input, Context context) {
		GetRatingRequest requestBody = null;
		try {
			requestBody = objectMapper.deserialise(input.getBody(), GetRatingRequest.class);
		} catch (Exception e) {
			// log e
			throw new RequestDeserialisationException(e);
		}

		HttpRequest<GetRatingRequest> request = new HttpRequest<>();
		request.setBody(requestBody);
		request.setHeaders(input.getHeaders());
		return requestHandler.handleGet(request);
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public void setRequestHandler(ConsultRatingRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}
}