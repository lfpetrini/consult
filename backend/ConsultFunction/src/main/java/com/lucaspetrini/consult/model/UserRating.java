package com.lucaspetrini.consult.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

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
@DynamoDbBean
public class UserRating {
	private String sku;
	private String user;
	private Long rating;
	private Long date;
	private String review;
	private Long version;

	public void setSku(String sku) {
		this.sku = sku;
	}

	@DynamoDbPartitionKey
	public String getSku() {
		return sku;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@DynamoDbSortKey
	@DynamoDbSecondaryPartitionKey(indexNames="GSIRating")
	public String getUser() {
		return user;
	}

	public void setRating(Long rating) {
		this.rating = rating;
	}

	@DynamoDbSecondarySortKey(indexNames="GSIRating")
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

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getVersion() {
		return version;
	}
}
