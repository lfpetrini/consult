package com.lucaspetrini.consult.exception;

/**
 * Database issue exception.
 */
public class DatabaseException extends ServiceException {
	private static final long serialVersionUID = 1L;

	/**
	 * Database issue exception.
	 * 
	 * @param cause cause of this exception.
	 */
	public DatabaseException(Throwable cause) {
		super(cause, "Database error.", 500);
	}

	/**
	 * Database issue exception.
	 * 
	 * @param short description of the issue.
	 */
	public DatabaseException(String message) {
		super(message, 500);
	}

	/**
	 * Database issue exception.
	 * 
	 * @param cause cause of this exception.
	 * @param short description of the issue.
	 */
	public DatabaseException(Throwable cause, String message) {
		super(cause, message, 500);
	}

}
