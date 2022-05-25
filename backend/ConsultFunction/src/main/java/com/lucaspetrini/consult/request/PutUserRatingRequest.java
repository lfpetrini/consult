package com.lucaspetrini.consult.request;

public class PutUserRatingRequest {

	private Long rating;
	private Long date;
	private String review;


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
