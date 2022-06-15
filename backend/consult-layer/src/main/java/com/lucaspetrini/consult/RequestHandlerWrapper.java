package com.lucaspetrini.consult;

import com.lucaspetrini.consult.handler.ConsultRequestHandler;

class RequestHandlerWrapper<I, O>  {
	private ConsultRequestHandler<I, O> handler;
	private Class<I> inputClass;
	
	public Class<I> getInputClass() {
		return inputClass;
	}
	
	public ConsultRequestHandler<I, O> getHandler() {
		return handler;
	}

	public RequestHandlerWrapper(ConsultRequestHandler<I, O> handler, Class<I> inputClass) {
		this.handler = handler;
		this.inputClass = inputClass;
	}
}
