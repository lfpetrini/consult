package com.lucaspetrini.consult.service;

import java.util.concurrent.ExecutionException;

import com.lucaspetrini.consult.exception.DatabaseException;
import com.lucaspetrini.consult.model.UserRating;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

public class DynamoDbUserRatingService implements UserRatingService {
	private static final String VERSION_PREFIX = "v0-";
	private static final String USER_RATINGS = "user_ratings";

	private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
	private String userRatingsTableName;
	private DynamoDbAsyncTable<UserRating> userRatingsTable;
	
	public DynamoDbUserRatingService() {
		initDefaults(null);
	}
	
	public DynamoDbUserRatingService(DynamoDbAsyncClient dynamoDbAsyncClient) {
		initDefaults(dynamoDbAsyncClient);
	}

	private void initDefaults(DynamoDbAsyncClient dynamoDbAsyncClient) {
		if(dynamoDbAsyncClient != null) {
			setDynamoDbAsyncClient(dynamoDbAsyncClient);
		}
		else {
			setDynamoDbAsyncClient(DynamoDbAsyncClient.create());
		}
		withUserRatingsTable(USER_RATINGS);
	}

	public DynamoDbUserRatingService withUserRatingsTable(String userRatingsTable) {
		this.userRatingsTableName = userRatingsTable;
		mapTables(dynamoDbEnhancedAsyncClient);
		return this;
	}

	private void setDynamoDbAsyncClient(DynamoDbAsyncClient dynamoDbAsyncClient) {
		this.dynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(dynamoDbAsyncClient).build();
		mapTables(dynamoDbEnhancedAsyncClient);
	}

	private void mapTables(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
		userRatingsTable = dynamoDbEnhancedAsyncClient.table(userRatingsTableName, TableSchema.fromBean(UserRating.class));
	}

	@Override
	public UserRating getByUserIdAndCode(String userId, String code) {
		Key key = Key.builder()
                .partitionValue(code)
                .sortValue(VERSION_PREFIX + userId)
                .build();
		try {
			return userRatingsTable.getItem(key).get();
		} catch (Exception e) {
			// TODO log it
			e.printStackTrace();
			throw new DatabaseException(e);
		}
	}

	@Override
	public UserRating put(UserRating capture) {
		// TODO Auto-generated method stub
		return null;
	}


}
