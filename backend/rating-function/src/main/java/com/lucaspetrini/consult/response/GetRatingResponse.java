package com.lucaspetrini.consult.response;

/**
 * GET response body for a rating request.
 */
public class GetRatingResponse {
	private String code;
	private Long date;
	private Long aggregated;
	private Long quantity;
	private Long version;
	private Long numberOfReviews;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public Long getAggregated() {
		return aggregated;
	}

	public void setAggregated(Long aggregated) {
		this.aggregated = aggregated;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getNumberOfReviews() {
		return numberOfReviews;
	}

	public void setNumberOfReviews(Long numberOfReviews) {
		this.numberOfReviews = numberOfReviews;
	}
}
