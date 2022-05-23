package com.lucaspetrini.consult.exception;

public class UnsupportedMethodException extends ServiceException {
	private static final long serialVersionUID = 1L;

	public UnsupportedMethodException(String method) {
		super("Unsupported HTTP method " + method + ".", 400);
	}

}
