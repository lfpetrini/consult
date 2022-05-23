package com.lucaspetrini.consult.exception;

public class UnsupportedContentTypeException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public UnsupportedContentTypeException(String contentType) {
		super("Unsupported Content-Type " + contentType + ".", 400);
	}

}
