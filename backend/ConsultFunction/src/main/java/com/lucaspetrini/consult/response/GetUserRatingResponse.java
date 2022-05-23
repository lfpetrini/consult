package com.lucaspetrini.consult.response;

public class GetUserRatingResponse {

	private String sku;
	private String user;
	private Integer rating;
	private Long date;
	private String review;

	public void setSku(String sku) {
		this.sku = sku;
	}
	
	public String getSku() {
		return sku;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public String getUser() {
		return user;
	}

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
