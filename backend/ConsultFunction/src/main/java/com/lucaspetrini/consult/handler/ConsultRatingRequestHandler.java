package com.lucaspetrini.consult.handler;

import com.lucaspetrini.consult.request.GetRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.GetRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;

/**
 * HTTP request handler for the Consult API operations on ratings.
 */
public interface ConsultRatingRequestHandler {

	/**
	 * Process a GET request for ratings.
	 * 
	 * @param request GET request.
	 * @return HTTP response.
	 */
	HttpResponse<GetRatingResponse> handleGet(HttpRequest<GetRatingRequest> request);

}
