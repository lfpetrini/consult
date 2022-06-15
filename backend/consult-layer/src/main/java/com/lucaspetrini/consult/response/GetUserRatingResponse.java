package com.lucaspetrini.consult.response;

/**
 * GET response body for a user rating request.
 */
public class GetUserRatingResponse {

	private String code;
	private String user;
	private Long rating;
	private Long date;
	private String review;

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setRating(Long rating) {
		this.rating = rating;
	}

	public Long getRating() {
		return rating;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public Long getDate() {
		return date;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public String getReview() {
		return review;
	}
}
