package com.lucaspetrini.consult.exception;

/**
 * Resource not found exception.
 */
public class ResourceNotFoundException extends ServiceException {
	private static final long serialVersionUID = 1L;

	/**
	 * Resource not found exception.
	 */
	public ResourceNotFoundException() {
		super("Resource not found.", 500);
	}
	
	/**
	 * Resource not found exception.
	 * 
	 * @param short description of the issue.
	 */
	public ResourceNotFoundException(String message) {
		super(message, 500);
	}

}
