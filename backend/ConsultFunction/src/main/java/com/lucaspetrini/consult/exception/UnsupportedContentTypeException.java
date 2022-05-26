package com.lucaspetrini.consult.exception;

/**
 * Unsupported content type exception.
 */
public class UnsupportedContentTypeException extends ServiceException {

	private static final long serialVersionUID = 1L;

	/**
	 * Unsupported content type exception.
	 * 
	 * @param contentType content-type that is not supported.
	 */
	public UnsupportedContentTypeException(String contentType) {
		super("Unsupported Content-Type " + contentType + ".", 400);
	}

}
