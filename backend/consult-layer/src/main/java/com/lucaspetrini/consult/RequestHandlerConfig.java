package com.lucaspetrini.consult;

import com.lucaspetrini.consult.auth.AuthenticationService;
import com.lucaspetrini.consult.exception.NotAllowedException;
import com.lucaspetrini.consult.handler.ConsultRequestHandler;
import com.lucaspetrini.consult.request.HttpRequest;
import com.lucaspetrini.consult.response.HttpResponse;

/**
 * Configuration class for a {@link ConsultRequestHandler}. This is necessary to
 * handle its generic types, and to secure specific handlers, when necessary.
 *
 * @param <I> model class of the request body to be handled.
 * @param <O> model class of the response body to be returned.
 */
class RequestHandlerConfig<I, O> {
	private ConsultRequestHandler<I, O> handler;
	private Class<I> inputClass;
	private AuthenticationService authenticationService;

	public Class<I> getInputClass() {
		return inputClass;
	}

	/**
	 * Process an HTTP request, only if it is authenticated by the authentication service.
	 *
	 * @param request request.
	 * @return response.
	 */
	public HttpResponse<O> execute(HttpRequest<I> request) {
		if (authenticationService != null && !authenticationService.isValid(request)) {
			throw new NotAllowedException();
		}
		return handler.handle(request);
	}

	/**
	 * Configuration class for a {@link ConsultRequestHandler}. This is necessary to
	 * handle its generic types, and to secure specific handlers, when necessary.
	 *
	 * @param handler               request handler.
	 * @param inputClass            model class of the request body to be handled.
	 * @param authenticationService authentication service.
	 */
	public RequestHandlerConfig(ConsultRequestHandler<I, O> handler, Class<I> inputClass,
			AuthenticationService authenticationService) {
		this.handler = handler;
		this.inputClass = inputClass;
		this.authenticationService = authenticationService;
	}

	/**
	 * Configuration class for a {@link ConsultRequestHandler}. This is necessary to
	 * handle its generic types, and to secure specific handlers, when necessary.
	 * This constructor does not secure its underlying handler (the authentication
	 * service validation will always return true).
	 *
	 * @param handler    request handler.
	 * @param inputClass model class of the request body to be handled.
	 */
	public RequestHandlerConfig(ConsultRequestHandler<I, O> handler, Class<I> inputClass) {
		this.handler = handler;
		this.inputClass = inputClass;
		this.authenticationService = (request) -> true;
	}
}
