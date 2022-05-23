package com.lucaspetrini.consult.request;

public class PutUserRatingRequest {

	private Integer rating;
	private Long date;
	private String review;


	public void setRating(Integer rating) {
		this.rating = rating;
	}
	
	public Integer getRating() {
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
