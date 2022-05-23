package com.lucaspetrini.consult.service;

import com.lucaspetrini.consult.model.UserRating;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

public class DynamoDbUserRatingService implements UserRatingService {

	private DynamoDbAsyncClient dynamoDbAsyncClient;
	public DynamoDbUserRatingService withDynamoDbAsyncClient(DynamoDbAsyncClient dynamoDbAsyncClient) {
		setDynamoDbAsyncClient(dynamoDbAsyncClient);
		return this;
	}

	private void setDynamoDbAsyncClient(DynamoDbAsyncClient dynamoDbAsyncClient) {
		this.dynamoDbAsyncClient = dynamoDbAsyncClient;
	}

	@Override
	public UserRating getByUserIdAndCode(String userId, String code) {
		return null;
	}

	@Override
	public UserRating put(UserRating capture) {
		// TODO Auto-generated method stub
		return null;
	}


}
