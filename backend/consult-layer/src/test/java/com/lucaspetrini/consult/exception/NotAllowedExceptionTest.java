package com.lucaspetrini.consult.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test {@link NotAllowedException}.
 */
public class NotAllowedExceptionTest {
	private static final String MESSAGE = "Another message.";
	private static final String DEFAULT_MESSAGE = "Not allowed.";
	private static final Integer STATUS_CODE = 403;
	private NotAllowedException exception;

	@Test
	public void testDefaultExceptionShortDescription() {
		// when
		exception = new NotAllowedException();

		// then
		assertEquals(DEFAULT_MESSAGE, exception.getShortDescription());
	}

	@Test
	public void testExceptionShortDescription() {
		// when
		exception = new NotAllowedException(MESSAGE);

		// then
		assertEquals(MESSAGE, exception.getShortDescription());
	}

	@Test
	public void testExceptionStatusCode() {
		// when
		exception = new NotAllowedException();

		// then
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}


}
