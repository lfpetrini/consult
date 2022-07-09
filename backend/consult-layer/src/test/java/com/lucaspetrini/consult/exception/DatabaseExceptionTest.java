package com.lucaspetrini.consult.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test {@link DatabaseException}.
 */
public class DatabaseExceptionTest {
	private static final String MESSAGE = "Cannot connect to database.";
	private static final Integer STATUS_CODE = 500;
	private static final Exception UNDERLYING_EXCEPTION = new RuntimeException();
	private DatabaseException exception;

	@Test
	public void testExceptionShortDescription() {
		// when
		exception = new DatabaseException(MESSAGE);
		
		// then
		assertEquals(MESSAGE, exception.getShortDescription());
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}

	@Test
	public void testUnderlyingException() {
		// when
		exception = new DatabaseException(UNDERLYING_EXCEPTION);
		
		// then
		assertEquals(UNDERLYING_EXCEPTION, exception.getCause());
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}

	@Test
	public void testUnderlyingExceptionAndDescription() {
		// when
		exception = new DatabaseException(UNDERLYING_EXCEPTION, MESSAGE);
		
		// then
		assertEquals(UNDERLYING_EXCEPTION, exception.getCause());
		assertEquals(MESSAGE, exception.getMessage());
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}

}
