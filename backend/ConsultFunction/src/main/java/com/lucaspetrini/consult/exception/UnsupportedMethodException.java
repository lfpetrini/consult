package com.lucaspetrini.consult.exception;

/**
 * Unsupported method exception.
 */
public class UnsupportedMethodException extends ServiceException {
	private static final long serialVersionUID = 1L;

	/**
	 * Unsupported method exception.
	 * 
	 * @param method method that is not supported.
	 */
	public UnsupportedMethodException(String method) {
		super("Unsupported HTTP method " + method + ".", 400);
	}

}
