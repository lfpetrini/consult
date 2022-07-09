package com.lucaspetrini.consult.auth;

import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.utils.ConsultConstants;

public class PathParamAuthenticationService implements AuthenticationService {

	@Override
	public boolean isValid(HttpRequest<?> request) {
		String pathUser = request.getPathParams().get(ConsultConstants.PATH_PARAM_USER_ID);
		String jwtUser = request.getContext().get(ConsultConstants.CONTEXT_AUTHORIZATION_JWT_USER);
		return pathUser != null && jwtUser != null && pathUser.equals(jwtUser);
	}

}
