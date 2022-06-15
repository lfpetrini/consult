package com.lucaspetrini.consult.mapper;

/**
 * An ObjectMapper is responsible for serialising and deserialising objects.
 */
public interface ObjectMapper {

	/**
	 * Deserialise an input String into an Object of the given type.
	 * 
	 * @param <T> type which the input string should be deserialised into.
	 * @param body string representation of the object to be deserialised.
	 * @param clazz class type which the input string should be deserialised into.
	 * @return
	 */
	<T> T deserialise(String body, Class<T> clazz);

	/**
	 * Serialise an object into a String.
	 * 
	 * @param <T> object type.
	 * @param object object to be serialised.
	 * @return string representation of the object.
	 */
	<T> String serialise(T object);

}
