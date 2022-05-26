package com.lucaspetrini.consult.handler;

import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.response.PutUserRatingResponse;

/**
 * HTTP request handler.
 */
public interface ConsultRequestHandler {

	/**
	 * Process a PUT request.
	 * 
	 * @param request PUT request.
	 * @return HTTP response.
	 */
	HttpResponse<PutUserRatingResponse> handlePut(HttpRequest<PutUserRatingRequest> request);

	/**
	 * Process a GET request.
	 * 
	 * @param request GET request.
	 * @return HTTP response.
	 */
	HttpResponse<GetUserRatingResponse> handleGet(HttpRequest<GetUserRatingRequest> request);

}
