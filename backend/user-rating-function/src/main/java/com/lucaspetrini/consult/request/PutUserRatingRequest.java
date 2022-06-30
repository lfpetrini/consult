package com.lucaspetrini.consult.request;

/**
 * PUT request body for a user rating request.
 */
public class PutUserRatingRequest {

	private Long rating;
	private String review;

	public void setRating(Long rating) {
		this.rating = rating;
	}

	public Long getRating() {
		return rating;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public String getReview() {
		return review;
	}

}
