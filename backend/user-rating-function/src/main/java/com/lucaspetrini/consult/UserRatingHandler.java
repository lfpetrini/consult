package com.lucaspetrini.consult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.lucaspetrini.consult.exception.RequestDeserialisationException;
import com.lucaspetrini.consult.exception.ServiceException;
import com.lucaspetrini.consult.exception.UnsupportedContentTypeException;
import com.lucaspetrini.consult.exception.UnsupportedMethodException;
import com.lucaspetrini.consult.handler.ConsultRequestHandler;
import com.lucaspetrini.consult.handler.ConsultUserRatingGetRequestHandler;
import com.lucaspetrini.consult.handler.ConsultUserRatingPutRequestHandler;
import com.lucaspetrini.consult.mapper.JsonObjectMapper;
import com.lucaspetrini.consult.mapper.ObjectMapper;
import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.service.DynamoDbUserRatingService;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Handler for requests to Lambda function.
 */
public class UserRatingHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserRatingHandler.class);
	private ObjectMapper objectMapper;
	private Map<HttpMethod, RequestHandlerWrapper<?,?>> handlerMap;
	
	public UserRatingHandler() {
		this.objectMapper = new JsonObjectMapper();
		DynamoDbUserRatingService userRatingService = new DynamoDbUserRatingService();
		addRequestHandlerMap(HttpMethod.GET, new ConsultUserRatingGetRequestHandler(userRatingService), GetUserRatingRequest.class);
		addRequestHandlerMap(HttpMethod.PUT, new ConsultUserRatingPutRequestHandler(userRatingService), PutUserRatingRequest.class);
	}

	/**
	 * Main request handler.
	 * 
	 * @param input APIGatewayProxyRequest event.
	 * @param context Lambda execution environment context.
	 */
	@Override
	public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent input, final Context context) {
		HttpResponse<?> response = null;
		String responseBody = null;
		try {
			validateHeaders(input.getHeaders());
			String method = input.getRequestContext().getHttp().getMethod();
			LOGGER.info("Handling method " + method);
			RequestHandlerWrapper<?, ?> handler = handlerMap.get(HttpMethod.valueOf(method.toUpperCase()));
			if(handler != null)
				response = handleRequest(input, handler, context);
			else {
				UnsupportedMethodException invalidMethodException = new UnsupportedMethodException(method);
				throw invalidMethodException ; // change to a different ServiceException
			}
			responseBody = objectMapper.serialise(response.getBody());
		}
		catch (Exception e) {
			LOGGER.error("Caught exception: " + e.getMessage());
			return buildErrorResponse(e);
		}
		Map<String, String> responseHeaders = new HashMap<>();
		responseHeaders.put(ConsultConstants.HEADER_CONTENT_TYPE, ConsultConstants.CONTENT_TYPE_JSON);
		APIGatewayV2HTTPResponse responseEvent = new APIGatewayV2HTTPResponse();
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
	private APIGatewayV2HTTPResponse buildErrorResponse(Exception e) {
		String errorMessage = ConsultConstants.UNHANDLED_EXCEPTION_ERROR_DESC;
		int statusCode = 500;
		if(e instanceof ServiceException) {
			errorMessage = ((ServiceException)e).getShortDescription();
			statusCode = ((ServiceException)e).getStatusCode();
		}
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		response.setBody("{\"errorDesc\":\"" + errorMessage + "\"}");
		response.setStatusCode(statusCode);
		response.setHeaders(Collections.singletonMap(ConsultConstants.HEADER_CONTENT_TYPE, ConsultConstants.CONTENT_TYPE_JSON));
		return response;
	}

	// TODO refactor
	private <I, O> HttpResponse<O> handleRequest(APIGatewayV2HTTPEvent input, RequestHandlerWrapper<I,O> handler, Context context) {
		I requestBody = null;
		try {
			requestBody = objectMapper.deserialise(input.getBody(), handler.getInputClass());
		} catch (Exception e) {
			LOGGER.error("Caught exception: " + e.getMessage());
			throw new RequestDeserialisationException(e);
		}

		HttpRequest<I> request = new HttpRequest<>();
		request.setBody(requestBody);
		request.setHeaders(input.getHeaders());
		request.setPathParams(input.getPathParameters());
		
		return handler.getHandler().handle(request);
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Map an HTTP Method to a {@link ConsultRequestHandler}.
	 * 
	 * @param <I> request model class type
	 * @param <O> response model class type
	 * @param method HTTP method to be handled.
	 * @param requestHandler request handler to handle incoming HTTP requests.
	 * @param requesetModelClass request model class type.
	 */
	public synchronized <I, O> void addRequestHandlerMap(HttpMethod method, ConsultRequestHandler<I, O> requestHandler, Class<I> requestModelClass) {
		if(this.handlerMap == null) {
			this.handlerMap = new HashMap<>();
		}
		this.handlerMap.put(method, new RequestHandlerWrapper<>(requestHandler, requestModelClass));
	}
}
