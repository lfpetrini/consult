package com.lucaspetrini.consult.handler;

import com.lucaspetrini.consult.request.GetUserRatingRequest;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.request.PutUserRatingRequest;
import com.lucaspetrini.consult.response.GetUserRatingResponse;
import com.lucaspetrini.consult.response.HttpResponse;
import com.lucaspetrini.consult.response.PutUserRatingResponse;

public interface ConsultRequestHandler {

	HttpResponse<PutUserRatingResponse> handlePut(HttpRequest<PutUserRatingRequest> request);

	HttpResponse<GetUserRatingResponse> handleGet(HttpRequest<GetUserRatingRequest> request);

}
