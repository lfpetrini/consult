package com.lucaspetrini.consult.handler;

import java.util.Map;

import com.lucaspetrini.consult.dao.UserRatingDao;
import com.lucaspetrini.consult.exception.ResourceNotFoundException;
import com.lucaspetrini.consult.model.UserRating;
import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.response.PutUserRatingResponse;
import com.lucaspetrini.consult.utils.ConsultConstants;

public class DefaultConsultRequestHandler implements ConsultRequestHandler {

	private UserRatingDao userRatingDao;

	@Override
	public HttpResponse<PutUserRatingResponse> handlePut(HttpRequest<PutUserRatingRequest> putUserRating) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse<GetUserRatingResponse> handleGet(HttpRequest<GetUserRatingRequest> request) {
		Map<String, String> pathParams = request.getPathParams();
		UserRating entity = userRatingDao.getByUserIdAndCode(pathParams.get(ConsultConstants.PATH_PARAM_USER_ID), pathParams.get(ConsultConstants.PATH_PARAM_CODE));
		if(entity == null) {
			throw new ResourceNotFoundException();
		}
		HttpResponse<GetUserRatingResponse> response = new HttpResponse<>();
		GetUserRatingResponse responseBody = covertToResponseBody(entity);
		response.setBody(responseBody);
		response.setStatusCode(200);
		return response;
	}

	private GetUserRatingResponse covertToResponseBody(UserRating entity) {
		GetUserRatingResponse body = new GetUserRatingResponse();
		body.setSku(entity.getSku());
		body.setUser(entity.getUser());
		body.setRating(entity.getRating());
		body.setDate(entity.getDate());
		body.setReview(entity.getReview());
		return body ;
	}

	public void setUserRatingDao(UserRatingDao userRatingDao) {
		this.userRatingDao = userRatingDao;
	}

}
