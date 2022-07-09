package com.lucaspetrini.consult.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test {@link UnsupportedContentTypeException}.
 */
public class UnsupportedContentTypeExceptionTest {
	private static final String CONTENT_TYPE = "application/cupcake";
	private static final Integer STATUS_CODE = 400;
	private static final String EXPECTED_DESCRIPTION = "Unsupported Content-Type application/cupcake.";
	private UnsupportedContentTypeException exception;

	@Test
	public void testExceptionShortDescription() {
		// when
		exception = new UnsupportedContentTypeException(CONTENT_TYPE);
		
		// then
		assertEquals(EXPECTED_DESCRIPTION, exception.getShortDescription());
	}

	@Test
	public void testExceptionStatusCode() {
		// when
		exception = new UnsupportedContentTypeException(CONTENT_TYPE);
		
		// then
		assertEquals(STATUS_CODE, exception.getStatusCode());
	}


}
