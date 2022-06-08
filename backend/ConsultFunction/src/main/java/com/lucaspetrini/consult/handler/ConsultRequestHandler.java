package com.lucaspetrini.consult.handler;

import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.HttpResponse;

/**
 * HTTP request handler for the Consult API operations.
 *
 * @param <I> Request type.
 * @param <O> Response type.
 */
public interface ConsultRequestHandler<I, O> {

	/**
	 * Process an HTTP request.
	 * @param request
	 * @return
	 */
	HttpResponse<O> handle(HttpRequest<I> request);

}
