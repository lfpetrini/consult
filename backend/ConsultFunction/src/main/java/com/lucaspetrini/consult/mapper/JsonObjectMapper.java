package com.lucaspetrini.consult.mapper;

import com.google.gson.Gson;

/**
 * Implementation of a {@link ObjectMapper} to handle JSON data format.
 *
 */
public class JsonObjectMapper implements ObjectMapper {
	private static final Gson GSON = new Gson();

	@Override
	public <T> T deserialise(String body, Class<T> clazz) {
		return GSON.fromJson(body, clazz);
	}

	@Override
	public <T> String serialise(T object) {
		return GSON.toJson(object);
	}

}
