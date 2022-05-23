package com.lucaspetrini.consult.exception;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private int statusCode;
	// TODO add custom headers?
	
	public ServiceException(Exception e, int statusCode) {
		super(e);
		this.statusCode = statusCode;
	}

	public ServiceException(String errorMessage, int statusCode) {
		this(new RuntimeException(errorMessage), statusCode);
	}

	public int getStatusCode() {
		return statusCode;
	}
	
	@Override
	public String getMessage() {
		// TODO check for null?
		return getCause().getMessage();
	}
}
