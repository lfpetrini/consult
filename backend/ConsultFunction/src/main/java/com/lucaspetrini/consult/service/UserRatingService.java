package com.lucaspetrini.consult.service;

import com.lucaspetrini.consult.service.model.UserRating;

/**
 * Service to handle persistence for {@link UserRating user ratings}.
 */
public interface UserRatingService {

	/**
	 * Get a {@link UserRating} by its user id and code.
	 * 
	 * @param userId user id.
	 * @param code code.
	 * @return user rating, or null if not found.
	 */
	UserRating getByUserIdAndCode(String userId, String code);

	/**
	 * Insert or update a {@link UserRating}.
	 * 
	 * @param userRating user rating to be inserted or updated.
	 * @return user rating after insertion/update.
	 */
	UserRating put(UserRating userRating);

}
