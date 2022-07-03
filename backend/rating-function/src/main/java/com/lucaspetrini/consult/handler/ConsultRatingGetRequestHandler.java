package com.lucaspetrini.consult.handler;

import java.util.Map;

import com.lucaspetrini.consult.exception.ResourceNotFoundException;
import com.lucaspetrini.consult.request.GetRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.GetRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.service.RatingService;
import com.lucaspetrini.consult.service.model.Rating;
import com.lucaspetrini.consult.utils.ConsultConstants;

/**
 * Default implementation of {@link ConsultRequestHandler} that delegates requests to an underlying
 * {@link RatingService}.
 */
public class ConsultRatingGetRequestHandler implements ConsultRequestHandler<GetRatingRequest, GetRatingResponse> {

	private RatingService ratingService;
	
	/**
	 * Default implementation of {@link ConsultRequestHandler} that delegates requests to an underlying
	 * {@link RatingService}.
	 * 
	 * @param ratingService rating service.
	 */
	public ConsultRatingGetRequestHandler() {}

	/**
	 * Default implementation of {@link ConsultRequestHandler} that delegates requests to an underlying
	 * {@link RatingService}.
	 * 
	 * @param ratingService rating service.
	 */
	public ConsultRatingGetRequestHandler(RatingService ratingService) {
		setRatingService(ratingService);
	}

	private GetRatingResponse covertToGetResponseBody(Rating entity) {
		GetRatingResponse body = new GetRatingResponse();
		body.setCode(entity.getSku());
		body.setDate(entity.getDate());
		body.setQuantity(entity.getQuantity());
		body.setAggregated(entity.getAggregated());
		body.setVersion(entity.getVersion());
		return body;
	}

	public void setRatingService(RatingService ratingService) {
		this.ratingService = ratingService;
	}

	@Override
	public HttpResponse<GetRatingResponse> handle(HttpRequest<GetRatingRequest> request) {
		Map<String, String> pathParams = request.getPathParams();
		Rating entity = ratingService.getByCode(pathParams.get(ConsultConstants.PATH_PARAM_CODE));
		if(entity == null) {
			throw new ResourceNotFoundException();
		}
		HttpResponse<GetRatingResponse> response = new HttpResponse<>();
		GetRatingResponse responseBody = covertToGetResponseBody(entity);
		response.setBody(responseBody);
		response.setStatusCode(200);
		return response;
	}

}
