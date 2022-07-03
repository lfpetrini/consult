package com.lucaspetrini.consult.handler;

import java.util.Map;

import com.lucaspetrini.consult.exception.ResourceNotFoundException;
import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.service.UserRatingService;
import com.lucaspetrini.consult.service.model.UserRating;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Default implementation of {@link ConsultRequestHandler} that delegates requests to an underlying
 * {@link UserRatingService}.
 */
public class ConsultUserRatingGetRequestHandler implements ConsultRequestHandler<GetUserRatingRequest, GetUserRatingResponse> {

	private UserRatingService userRatingService;

	/**
	 * Default implementation of {@link ConsultRequestHandler} that delegates requests to an underlying
	 * {@link UserRatingService}.
	 */
	public ConsultUserRatingGetRequestHandler() {}

	/**
	 * Default implementation of {@link ConsultRequestHandler} that delegates requests to an underlying
	 * {@link UserRatingService}.
	 */
	public ConsultUserRatingGetRequestHandler(UserRatingService ratingService) {
		setUserRatingService(ratingService);
	}

	private GetUserRatingResponse covertToGetResponseBody(UserRating entity) {
		GetUserRatingResponse body = new GetUserRatingResponse();
		body.setCode(entity.getSku());
		body.setUser(entity.getUser());
		body.setRating(entity.getRating());
		body.setDate(entity.getDate());
		body.setReview(entity.getReview());
		body.setVersion(entity.getVersion());
		return body ;
	}

	public void setUserRatingService(UserRatingService userRatingService) {
		this.userRatingService = userRatingService;
	}

	@Override
	public HttpResponse<GetUserRatingResponse> handle(HttpRequest<GetUserRatingRequest> request) {
		Map<String, String> pathParams = request.getPathParams();
		UserRating entity = userRatingService.getByUserIdAndCode(pathParams.get(ConsultConstants.PATH_PARAM_USER_ID), pathParams.get(ConsultConstants.PATH_PARAM_CODE));
		if(entity == null) {
			throw new ResourceNotFoundException();
		}
		HttpResponse<GetUserRatingResponse> response = new HttpResponse<>();
		GetUserRatingResponse responseBody = covertToGetResponseBody(entity);
		response.setBody(responseBody);
		response.setStatusCode(200);
		return response;
	}

}
