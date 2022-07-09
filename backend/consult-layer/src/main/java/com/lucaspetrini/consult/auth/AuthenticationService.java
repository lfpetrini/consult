package com.lucaspetrini.consult.auth;

import com.lucaspetrini.consult.request.HttpRequest;

public interface AuthenticationService {

	boolean isValid(HttpRequest<?> request);

}
