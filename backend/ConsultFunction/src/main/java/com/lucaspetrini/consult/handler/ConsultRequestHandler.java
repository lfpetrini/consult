package com.lucaspetrini.consult.handler;

import java.util.Map;

import com.lucaspetrini.consult.request.GetUserRatingsRequest;
import com.lucaspetrini.consult.request.PutUserRatingsRequest;
import com.lucaspetrini.consult.response.GetUserRatingsResponse;
import com.lucaspetrini.consult.response.PutUserRatingsResponse;

public interface ConsultRequestHandler {

	PutUserRatingsResponse handlePut(Map<String, String> headers, PutUserRatingsRequest putUserRatings);

	GetUserRatingsResponse handleGet(Map<String, String> headers, GetUserRatingsRequest getUserRatings);

}
