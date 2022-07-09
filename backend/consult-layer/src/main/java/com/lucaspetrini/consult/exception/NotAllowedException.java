package com.lucaspetrini.consult.exception;

/**
 * Request not allowed exception.
 */
public class NotAllowedException extends ServiceException {
	private static final long serialVersionUID = 1L;

	/**
	 * Request not allowed exception.
	 *
	 * @param message short description of the issue.
	 */
	public NotAllowedException(String message) {
		super(message, 403);
	}

	/**
	 * Request not allowed exception.
	 */
	public NotAllowedException() {
		this("Not allowed.");
	}


}
