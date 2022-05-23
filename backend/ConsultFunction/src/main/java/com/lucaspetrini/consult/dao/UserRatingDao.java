package com.lucaspetrini.consult.dao;

import com.lucaspetrini.consult.model.UserRating;

public interface UserRatingDao {

	UserRating getByUserIdAndCode(String userId, String code);

}
