package com.lucaspetrini.consult.utils;

/**
 * Constants for the application.
 */
public final class ConsultConstants {
	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_GET = "GET";
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String UNHANDLED_EXCEPTION_ERROR_DESC = "Internal server error.";
	public static final String PATH_PARAM_USER_ID = "id";
	public static final String PATH_PARAM_CODE = "code";
	public static final String CONTEXT_AUTHORIZATION_JWT_USER = "AuthorizationJwtUser";
	public static final String JWT_CLAIM_USERNAME = "username";

	private ConsultConstants() {}
}
