package com.lucaspetrini.consult;

import com.lucaspetrini.consult.handler.ConsultRatingGetRequestHandler;
import com.lucaspetrini.consult.request.GetRatingRequest;
import com.lucaspetrini.consult.service.DynamoDbRatingService;

/**
 * Handler for requests to Lambda function.
 */
public class RatingHandler extends AbstractRequestHandler {

	public RatingHandler() {
		super();
		DynamoDbRatingService ratingService = new DynamoDbRatingService();
		addRequestHandlerMap(HttpMethod.GET, new ConsultRatingGetRequestHandler(ratingService), GetRatingRequest.class);
	}
}
