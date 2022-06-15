package com.lucaspetrini.consult.utils;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

/**
 * JUnit extension that starts an in-memory DynamoDB server.
 */
public class DynamoDBExtension implements BeforeAllCallback, AfterAllCallback {
	public static final int SERVER_PORT = 8123;
	private DynamoDBProxyServer server;

	public DynamoDBExtension() {
		System.setProperty("sqlite4java.library.path", "native-libs");
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		server = ServerRunner
				.createServerFromCommandLineArgs(new String[] { "-inMemory", "-port", String.valueOf(SERVER_PORT) });
		server.start();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
