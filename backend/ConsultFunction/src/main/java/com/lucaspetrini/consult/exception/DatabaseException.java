package com.lucaspetrini.consult.exception;

public class DatabaseException extends ServiceException {
	private static final long serialVersionUID = 1L;

	public DatabaseException(Exception e) {
		super(e, 500);
	}

}
