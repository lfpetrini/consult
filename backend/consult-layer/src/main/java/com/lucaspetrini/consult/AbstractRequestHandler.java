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
import com.lucaspetrini.consult.auth.AuthenticationService;
import com.lucaspetrini.consult.exception.RequestDeserialisationException;
import com.lucaspetrini.consult.exception.ServiceException;
import com.lucaspetrini.consult.exception.UnsupportedContentTypeException;
import com.lucaspetrini.consult.exception.UnsupportedMethodException;
import com.lucaspetrini.consult.handler.ConsultRequestHandler;
import com.lucaspetrini.consult.mapper.JsonObjectMapper;
import com.lucaspetrini.consult.mapper.ObjectMapper;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Generic handler for JSON requests.
 */
public abstract class AbstractRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	private ObjectMapper objectMapper;
	private Map<HttpMethod, RequestHandlerConfig<?,?>> handlerMap;

	/**
	 * Abstract handler for JSON requests.
	 */
	public AbstractRequestHandler() {
		this.objectMapper = new JsonObjectMapper();
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
			logger.info("Handling method " + method);
			RequestHandlerConfig<?, ?> handler = handlerMap.get(HttpMethod.valueOf(method.toUpperCase()));
			if(handler != null)
				response = handleRequest(input, handler, context);
			else {
				UnsupportedMethodException invalidMethodException = new UnsupportedMethodException(method);
				throw invalidMethodException ; // change to a different ServiceException
			}
			responseBody = objectMapper.serialise(response.getBody());
		}
		catch (Exception e) {
			logger.error("Caught exception: " + e.getMessage());
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

	/**
	 * Check if content type is valid (at the moment, only application/json is supported).
	 *
	 * @param headers
	 * @throws UnsupportedContentTypeException
	 */
	protected void validateHeaders(Map<String, String> headers) {
		// TODO extract/refactor ? Then use a strategy and/or factory method
		if(headers == null)
			return;
		// get case insensitive content-type or defaults to application/json if not found
		String contentType = headers.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(ConsultConstants.HEADER_CONTENT_TYPE)).map(e -> e.getValue()).findFirst().orElse(ConsultConstants.CONTENT_TYPE_JSON);
		if(!ConsultConstants.CONTENT_TYPE_JSON.equals(contentType)) {
			throw new UnsupportedContentTypeException(contentType);
		}
	}

	/**
	 * Build a generic error {@link APIGatewayV2HTTPResponse response} based on the expection passed as parameter.
	 *
	 * @param e exception responsible for the error.
	 * @return error response
	 */
	protected APIGatewayV2HTTPResponse buildErrorResponse(Exception e) {
		// TODO extract/refactor ? Then use a strategy and/or factory method
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

	/**
	 * Delegates the incoming request to its specific {@link RequestHandlerConfig handler}.
	 *
	 * @param <I> class model of request body.
	 * @param <O> class model of response body.
	 * @param input incoming request.
	 * @param handler wrapper class containing a request handler.
	 * @param context AWS runtime context.
	 * @return HTTP response.
	 */
	protected <I, O> HttpResponse<O> handleRequest(APIGatewayV2HTTPEvent input, RequestHandlerConfig<I,O> handler, Context context) {
		I requestBody = null;
		try {
			requestBody = objectMapper.deserialise(input.getBody(), handler.getInputClass());
		} catch (Exception e) {
			logger.error("Caught exception: " + e.getMessage());
			throw new RequestDeserialisationException(e);
		}

		HttpRequest<I> request = new HttpRequest<>();
		request.setBody(requestBody);
		request.setHeaders(input.getHeaders());
		request.setPathParams(input.getPathParameters());
		request.setContext(buildRequestContext(input));

		return handler.execute(request);
	}

	private Map<String, String> buildRequestContext(APIGatewayV2HTTPEvent input) {
		Map<String, String> context = new HashMap<>();
		if(input.getRequestContext() != null &&
				input.getRequestContext().getAuthorizer() != null &&
				input.getRequestContext().getAuthorizer().getJwt() != null) {
			context.put(ConsultConstants.CONTEXT_AUTHORIZATION_JWT_USER, input.getRequestContext().getAuthorizer().getJwt().getClaims().get(ConsultConstants.JWT_CLAIM_USERNAME));
		}
		return context;
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
	public <I, O> void addRequestHandlerMap(HttpMethod method, ConsultRequestHandler<I, O> requestHandler, Class<I> requestModelClass) {
		addRequestHandlerMap(method, requestHandler, requestModelClass, (request) -> true);
	}


	/**
	 * Map an HTTP Method to a {@link ConsultRequestHandler}.
	 *
	 * @param <I> request model class type
	 * @param <O> response model class type
	 * @param method HTTP method to be handled.
	 * @param requestHandler request handler to handle incoming HTTP requests.
	 * @param requesetModelClass request model class type.
	 * @param authenticationService authentication service responsible for securing the request handler.
	 */
	public synchronized <I, O> void addRequestHandlerMap(HttpMethod method, ConsultRequestHandler<I, O> requestHandler, Class<I> requestModelClass, AuthenticationService authenticationService) {
		if(this.handlerMap == null) {
			this.handlerMap = new HashMap<>();
		}
		this.handlerMap.put(method, new RequestHandlerConfig<>(requestHandler, requestModelClass, authenticationService));
	}
}
