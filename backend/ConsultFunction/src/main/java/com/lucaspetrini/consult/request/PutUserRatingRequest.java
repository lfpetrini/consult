package com.lucaspetrini.consult.request;

public class PutUserRatingRequest {

	private String sku;
	private String user;
	private Integer rating;

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

}
