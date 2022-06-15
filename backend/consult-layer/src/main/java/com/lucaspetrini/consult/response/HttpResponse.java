package com.lucaspetrini.consult.response;

import java.util.Map;

/**
 * Base class for http responses.
 * 
 * @param <T> type of the response body.
 */
public class HttpResponse<T> {

	private T responseBody;
	private Map<String, String> headers;
	private int statusCode;

	public T getBody() {
		return responseBody;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setBody(T responseBody) {
		this.responseBody = responseBody;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers; 
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
