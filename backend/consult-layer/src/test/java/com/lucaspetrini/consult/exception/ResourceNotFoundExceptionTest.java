package com.lucaspetrini.consult.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test {@link ResourceNotFoundException}.
 */
public class ResourceNotFoundExceptionTest {
	private static final String MESSAGE = "Resource not found message";
	private static final String DEFAULT_MESSAGE = "Resource not found.";
	private static final Integer STATUS_CODE = 404;
	private ResourceNotFoundException exception;

	@Test
	public void testDefaultExceptionShortDescription() {
		// when
		exception = new ResourceNotFoundException();

		// then
		assertEquals(DEFAULT_MESSAGE, exception.getShortDescription());
	}

	@Test
	public void testExceptionShortDescription() {
		// when
		exception = new ResourceNotFoundException(MESSAGE);

		// then
		assertEquals(MESSAGE, exception.getShortDescription());
	}

	@Test
	public void testExceptionStatusCode() {
		// when
		exception = new ResourceNotFoundException();

		// then
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}


}
