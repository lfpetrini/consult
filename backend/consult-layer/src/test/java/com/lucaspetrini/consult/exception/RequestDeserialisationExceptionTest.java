package com.lucaspetrini.consult.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test {@link RequestDeserialisationException}.
 */
public class RequestDeserialisationExceptionTest {
	private static final String MESSAGE = "Cannot deserialise request.";
	private static final Integer STATUS_CODE = 400;
	private static final Exception UNDERLYING_EXCEPTION = new RuntimeException();
	private RequestDeserialisationException exception;

	@Test
	public void testExceptionShortDescription() {
		// when
		exception = new RequestDeserialisationException(MESSAGE);

		// then
		assertEquals(MESSAGE, exception.getShortDescription());
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}

	@Test
	public void testUnderlyingException() {
		// when
		exception = new RequestDeserialisationException(UNDERLYING_EXCEPTION);
		
		// then
		assertEquals(UNDERLYING_EXCEPTION, exception.getCause());
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}

	@Test
	public void testUnderlyingExceptionAndDescription() {
		// when
		exception = new RequestDeserialisationException(UNDERLYING_EXCEPTION, MESSAGE);
		
		// then
		assertEquals(UNDERLYING_EXCEPTION, exception.getCause());
		assertEquals(MESSAGE, exception.getMessage());
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}

}
