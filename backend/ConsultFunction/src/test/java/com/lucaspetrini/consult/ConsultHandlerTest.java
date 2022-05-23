package com.lucaspetrini.consult;

import org.junit.Before;
import org.junit.Test;

public class ConsultHandlerTest {

	@Before
	public void setUp() {
		// SQLite for DynamoDBLocal
		System.setProperty("sqlite4java.library.path", "native-libs");
	}
	
	@Test
	public void successfulResponse() {
		/*
		 * ConsultHandler app = new ConsultHandler(); GatewayResponse result =
		 * (GatewayResponse) app.handleRequest(null, null);
		 * assertEquals(result.getStatusCode(), 200);
		 * assertEquals(result.getHeaders().get("Content-Type"), "application/json");
		 * String content = result.getBody(); assertNotNull(content);
		 * assertTrue(content.contains("\"message\""));
		 * assertTrue(content.contains("\"hello world\""));
		 * assertTrue(content.contains("\"location\""));
		 */
	}
}
