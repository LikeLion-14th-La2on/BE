package com.dotorich.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.dotorich.common.exception.GlobalExceptionHandler;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void 잘못된_요청은_500이_아니라_4xx() {
		var badBody = handler.handleBadRequest(
			new HttpMessageNotReadableException("본문 없음", mock(HttpInputMessage.class)));
		assertEquals(400, badBody.getStatusCode().value());
		assertEquals("E400", badBody.getBody().code());

		var noPath = handler.handleNoResource(new NoResourceFoundException(HttpMethod.GET, "/api/v1/nope", "api/v1/nope"));
		assertEquals(404, noPath.getStatusCode().value());

		var badMethod = handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("PATCH"));
		assertEquals(405, badMethod.getStatusCode().value());
		assertEquals("E405", badMethod.getBody().code());
	}
}
