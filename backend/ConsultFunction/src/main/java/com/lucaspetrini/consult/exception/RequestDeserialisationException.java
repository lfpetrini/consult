package com.lucaspetrini.consult.exception;

public class RequestDeserialisationException extends ServiceException {
	private static final long serialVersionUID = 1L;

	public RequestDeserialisationException(Exception e){
		super(e, 400);
	}
	
	@Override
	public String getMessage() {
		return "Cannot deserialise request body.";
	}

}
