package com.lucaspetrini.consult;

import com.lucaspetrini.consult.auth.PathParamAuthenticationService;
import com.lucaspetrini.consult.handler.ConsultUserRatingGetRequestHandler;
import com.lucaspetrini.consult.handler.ConsultUserRatingPutRequestHandler;
import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.service.DynamoDbUserRatingService;

/**
 * Handler for requests to Lambda function.
 */
public class UserRatingHandler extends AbstractRequestHandler {

	public UserRatingHandler() {
		super();
		DynamoDbUserRatingService userRatingService = new DynamoDbUserRatingService();
		// public GET
		addRequestHandlerMap(HttpMethod.GET, new ConsultUserRatingGetRequestHandler(userRatingService), GetUserRatingRequest.class);
		// public secured via path param (user must match JWT username)
		addRequestHandlerMap(HttpMethod.PUT, new ConsultUserRatingPutRequestHandler(userRatingService), PutUserRatingRequest.class, new PathParamAuthenticationService());
	}

}
