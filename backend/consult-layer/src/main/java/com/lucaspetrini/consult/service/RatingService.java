package com.lucaspetrini.consult.service;

import com.lucaspetrini.consult.service.model.Rating;

/**
 * Service to handle persistence for {@link Rating ratings}.
 */
public interface RatingService {

	/**
	 * Get a {@link Rating} by its code.
	 * 
	 * @param code code.
	 * @return rating, or null if not found.
	 */
	Rating getByCode(String code);

}
