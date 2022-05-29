package com.lucaspetrini.consult.exception;

/**
 * Base class for known service exceptions, containing a user friendly description and an HTTP status
 * code related to the issue. Service exceptions should be handled in the main handler.<br>
 * Child exceptions should override {@link #getShortDescription()} or set it via super constructors,
 * to provide meaningful, user-friendly descriptions about the issues.
 */
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private int statusCode;
	private String shortDescription;
	// TODO add custom headers?

	/**
	 * Base class for known service exceptions, containing a user friendly description and an HTTP status
	 * code related to the issue. Service exceptions should be handled in the main handler.
	 * 
	 * @param cause cause of this exception.
	 * @param statusCode HTTP status code.
	 */
	public ServiceException(Throwable cause, int statusCode) {
		super(cause);
		this.statusCode = statusCode;
		this.shortDescription = "Unknown error.";
	}

	/**
	 * Base class for known service exceptions, containing a user friendly description and an HTTP status
	 * code related to the issue. Service exceptions should be handled in the main handler.
	 * 
	 * @param cause cause of this exception.
	 * @param shortDescription short description of this exception.
	 * @param statusCode statusCode HTTP status code.
	 */
	public ServiceException(String shortDescription, int statusCode) {
		super(shortDescription);
		this.shortDescription = shortDescription;
		this.statusCode = statusCode;
	}

	/**
	 * Base class for known service exceptions, containing a user friendly description and an HTTP status
	 * code related to the issue. Service exceptions should be handled in the main handler.
	 * 
	 * @param shortDescription short description of this exception.
	 * @param statusCode statusCode HTTP status code.
	 */
	public ServiceException(Throwable cause, String shortDescription, int statusCode) {
		super(shortDescription, cause);
		this.statusCode = statusCode;
		this.shortDescription = shortDescription;
	}

	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Meaningful, user-friendly description about the issue.
	 * 
	 * @return short description about the issue.
	 */
	public String getShortDescription() {
		return shortDescription;
	}
}
