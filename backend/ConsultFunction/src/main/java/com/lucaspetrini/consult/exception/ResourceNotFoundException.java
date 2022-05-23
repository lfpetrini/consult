package com.lucaspetrini.consult.exception;

public class ResourceNotFoundException extends ServiceException {

	public ResourceNotFoundException() {
		super("Resource not found.", 404);
	}

	public ResourceNotFoundException(String errorMessage) {
		super(errorMessage, 404);
	}

	private static final long serialVersionUID = 1L;

}
