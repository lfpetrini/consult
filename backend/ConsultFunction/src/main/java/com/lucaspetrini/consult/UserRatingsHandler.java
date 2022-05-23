package com.lucaspetrini.consult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.lucaspetrini.consult.exception.RequestDeserialisationException;
import com.lucaspetrini.consult.exception.ServiceException;
import com.lucaspetrini.consult.exception.UnsupportedContentTypeException;
import com.lucaspetrini.consult.handler.ConsultRequestHandler;
import com.lucaspetrini.consult.mapper.JsonRequestMapper;
import com.lucaspetrini.consult.request.GetUserRatingsRequest;
import com.lucaspetrini.consult.request.PutUserRatingsRequest;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Handler for requests to Lambda function.
 */
public class UserRatingsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private JsonRequestMapper requestMapper;
	private ConsultRequestHandler requestHandler;

	/**
	 * Main request handler.
	 * 
	 * @param input APIGatewayProxyRequest event.
	 * @param context Lambda execution environment context.
	 */
	@Override
	public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
		String responseBody = null;
		int statusCode = 200;
		try {
			validateHeaders(input.getHeaders());
			String method = input.getHttpMethod();
			switch (method) {
			case ConsultConstants.HTTP_METHOD_PUT:
				responseBody = handlePut(input, context);
				statusCode = 201;
				break;
			case ConsultConstants.HTTP_METHOD_GET:
				responseBody = handleGet(input, context);
				break;
			default:
				ServiceException invalidMethodException = new ServiceException("Unsupported HTTP method " + method + ".", 400);
				throw invalidMethodException ; // change to a different ServiceException
			}
		}
		catch (Exception e) {
			return buildErrorResponse(e);
		}

		Map<String, String> responseHeaders = new HashMap<>();
		responseHeaders.put("Content-Type", "application/json");
		responseHeaders.put("X-Custom-Header", "application/json");
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setHeaders(responseHeaders);
		response.setBody(responseBody);
		response.setStatusCode(statusCode);
		return response;
	}

	private void validateHeaders(Map<String, String> headers) {
		if(headers == null)
			return;
		// get case insensitive content-type or defaults to application/json if not found
		String contentType = headers.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(ConsultConstants.HEADER_CONTENT_TYPE)).map(e -> e.getValue()).findFirst().orElse(ConsultConstants.CONTENT_TYPE_JSON);
		if(!ConsultConstants.CONTENT_TYPE_JSON.equals(contentType)) {
			throw new UnsupportedContentTypeException(contentType);
		}
	}

	private APIGatewayProxyResponseEvent buildErrorResponse(Exception e) {
		String errorMessage = ConsultConstants.UNHANDLED_EXCEPTION_ERROR_DESC;
		int statusCode = 500;
		if(e instanceof ServiceException) {
			errorMessage = ((ServiceException)e).getMessage();
			statusCode = ((ServiceException)e).getStatusCode();
		}
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setBody("{\"errorDesc\":\"" + errorMessage + "\"}");
		response.setStatusCode(statusCode);
		response.setHeaders(Collections.singletonMap(ConsultConstants.HEADER_CONTENT_TYPE, ConsultConstants.CONTENT_TYPE_JSON));
		return response;
	}

	private String handlePut(APIGatewayProxyRequestEvent input, Context context) {
		PutUserRatingsRequest request = null;
		try {
			request = requestMapper.deserialise(input.getBody(), PutUserRatingsRequest.class);
		} catch (Exception e) {
			// log e
			throw new RequestDeserialisationException(e);
		}
		
		return requestMapper.serialise(requestHandler.handlePut(input.getHeaders(), request));
	}

	private String handleGet(APIGatewayProxyRequestEvent input, Context context) {
		GetUserRatingsRequest request = null;
		try {
			request = requestMapper.deserialise(input.getBody(), GetUserRatingsRequest.class);
		} catch (Exception e) {
			// log e
			throw new RequestDeserialisationException(e);
		}
		return requestMapper.serialise(requestHandler.handleGet(input.getHeaders(), request));
	}

	private String getPageContents(String address) throws IOException {
		URL url = new URL(address);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
			return br.lines().collect(Collectors.joining(System.lineSeparator()));
		}
	}

	public void setRequestMapper(JsonRequestMapper requestMapper) {
		this.requestMapper = requestMapper;
	}

	public void setRequestHandler(ConsultRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}
}
