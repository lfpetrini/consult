package com.lucaspetrini.consult.handler;

import java.util.Map;

import com.lucaspetrini.consult.exception.ResourceNotFoundException;
import com.lucaspetrini.consult.model.UserRating;
import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.response.PutUserRatingResponse;
import com.lucaspetrini.consult.service.UserRatingService;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Default implementation of {@link ConsultRequestHandler} that delegates requests to an underlying
 * {@link UserRatingService}.
 */
public class DefaultConsultRequestHandler implements ConsultRequestHandler {

	private UserRatingService userRatingService;

	@Override
	public HttpResponse<PutUserRatingResponse> handlePut(HttpRequest<PutUserRatingRequest> request) {
		UserRating rating = buildUserRating(request);
		UserRating newRating = userRatingService.put(rating);
		HttpResponse<PutUserRatingResponse> response = new HttpResponse<>();
		PutUserRatingResponse responseBody = covertToPutResponseBody(newRating);
		response.setBody(responseBody);
		response.setStatusCode(200);
		return response;
	}

	private UserRating buildUserRating(HttpRequest<PutUserRatingRequest> request) {
		Map<String, String> pathParams = request.getPathParams();
		UserRating rating = new UserRating();
		rating.setUser(pathParams.get(ConsultConstants.PATH_PARAM_USER_ID));
		rating.setSku(pathParams.get(ConsultConstants.PATH_PARAM_CODE));
		rating.setDate(request.getBody().getDate());
		rating.setRating(request.getBody().getRating());
		rating.setReview(request.getBody().getReview());
		return rating;
	}

	@Override
	public HttpResponse<GetUserRatingResponse> handleGet(HttpRequest<GetUserRatingRequest> request) {
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

	private GetUserRatingResponse covertToGetResponseBody(UserRating entity) {
		GetUserRatingResponse body = new GetUserRatingResponse();
		body.setSku(entity.getSku());
		body.setUser(entity.getUser());
		body.setRating(entity.getRating());
		body.setDate(entity.getDate());
		body.setReview(entity.getReview());
		return body ;
	}

	private PutUserRatingResponse covertToPutResponseBody(UserRating rating) {
		PutUserRatingResponse response = new PutUserRatingResponse();
		response.setUser(rating.getUser());
		response.setCode(rating.getSku());
		response.setDate(rating.getDate());
		response.setRating(rating.getRating());
		response.setReview(rating.getReview());
		response.setVersion(rating.getVersion());
		return response;
	}

	public void setUserRatingService(UserRatingService userRatingService) {
		this.userRatingService = userRatingService;
	}

}
