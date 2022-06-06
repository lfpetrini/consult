package com.lucaspetrini.consult.handler;

import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.response.PutUserRatingResponse;

/**
 * HTTP request handler for the Consult API operations on user ratings.
 */
public interface ConsultUserRatingRequestHandler {

	/**
	 * Process a PUT request for user ratings.
	 * 
	 * @param request PUT request.
	 * @return HTTP response.
	 */
	HttpResponse<PutUserRatingResponse> handlePut(HttpRequest<PutUserRatingRequest> request);

	/**
	 * Process a GET request for user ratings.
	 * 
	 * @param request GET request.
	 * @return HTTP response.
	 */
	HttpResponse<GetUserRatingResponse> handleGet(HttpRequest<GetUserRatingRequest> request);

}
