package com.lucaspetrini.consult.exception;

/**
 * Request deserialisation exception.
 */
public class RequestDeserialisationException extends ServiceException {
	private static final long serialVersionUID = 1L;

	/**
	 * Request deserialisation exception.
	 * 
	 * @param cause cause of this exception.
	 */
	public RequestDeserialisationException(Throwable cause) {
		super(cause, "Cannot deserialise request body.", 400);
	}
	
	/**
	 * Request deserialisation exception.
	 * 
	 * @param short description of the issue.
	 */
	public RequestDeserialisationException(String message) {
		super(message, 400);
	}
	
	/**
	 * Request deserialisation exception.
	 * 
	 * @param cause cause of this exception.
	 * @param short description of the issue.
	 */
	public RequestDeserialisationException(Throwable cause, String message) {
		super(cause, message, 400);
	}

}
