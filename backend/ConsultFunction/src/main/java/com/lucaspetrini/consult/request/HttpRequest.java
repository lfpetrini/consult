package com.lucaspetrini.consult.request;

import java.util.Map;

/**
 * Base class for http requests.
 * 
 * @param <T> type of the request body.
 */
public class HttpRequest<T> {

	private T requestBody;
	private Map<String, String> headers;
	private Map<String, String> pathParams;

	public T getBody() {
		return requestBody;
	}

	public void setBody(T requestBody) {
		this.requestBody = requestBody;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setPathParams(Map<String, String> pathParams) {
		this.pathParams = pathParams;
	}

	public Map<String, String> getPathParams() {
		return pathParams;
	}

}
