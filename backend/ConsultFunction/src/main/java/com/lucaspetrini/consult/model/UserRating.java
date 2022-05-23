package com.lucaspetrini.consult.model;

/**
 * A user rating consists of the following properties:
 * 
 * <ul>
 * <li>the code (SKU) subject of the rating</li>
 * <li>the user id of the reviewer</li>
 * <li>the rating (1 to 10)</li>
 * <li>the date for the rating</li>
 * <li>the review</li>
 * <li>the version of the user rating</li>
 * </ul>
 */
public class UserRating {
	private String sku;
	private String user;
	private Integer rating;
	private Long date;
	private String review;
	private Long version;

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

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getVersion() {
		return version;
	}
}
