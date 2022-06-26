package com.lucaspetrini.consult.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.lucaspetrini.consult.exception.UnsupportedMethodException;

/**
 * Test {@link UnsupportedMethodException}.
 */
public class UnsupportedMethodExceptionTest {
	private static final String METHOD = "DELETE";
	private static final Integer STATUS_CODE = 400;
	private static final String EXPECTED_DESCRIPTION = "Unsupported HTTP method DELETE.";
	private UnsupportedMethodException exception;

	@Test
	public void testExceptionShortDescription() {
		// when
		exception = new UnsupportedMethodException(METHOD);
		
		// then
		assertEquals(EXPECTED_DESCRIPTION, exception.getShortDescription());
	}

	@Test
	public void testExceptionStatusCode() {
		// when
		exception = new UnsupportedMethodException(METHOD);
		
		// then
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}


}
