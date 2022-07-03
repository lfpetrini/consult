package com.lucaspetrini.consult.service.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * A rating consists of the following properties:
 * 
 * <ul>
 * <li>the code (SKU) subject of the rating</li>
 * <li>the date on which it was last updated</li>
 * <li>the aggregated ratings value for the SKU</li>
 * <li>the total number of ratings for the SKU</li>
 * <li>the item version</li>
 * </ul>
 */
@DynamoDbBean
public class Rating {
	private String sku;
	private Long date;
	private Long aggregated;
	private Long quantity;
	private Long numberOfReviews;
	private Long version;

	public void setSku(String sku) {
		this.sku = sku;
	}

	@DynamoDbPartitionKey
	@DynamoDbSecondaryPartitionKey(indexNames="LSIDate")
	public String getSku() {
		return sku;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@DynamoDbSortKey
	public Long getVersion() {
		return version;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	@DynamoDbSecondarySortKey(indexNames="LSIDate")
	public Long getDate() {
		return date;
	}

	public void setAggregated(Long aggregated) {
		this.aggregated = aggregated;
	}

	public Long getAggregated() {
		return aggregated;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setNumberOfReviews(Long numberOfReviews) {
		this.numberOfReviews = numberOfReviews;
	}

	public Long getNumberOfReviews() {
		return numberOfReviews;
	}
}
