package com.lucaspetrini.consult.mapper;

public interface JsonRequestMapper {

	<T> T deserialise(String body, Class<T> clazz);

	<T> String serialise(T validPutResponse);

}
