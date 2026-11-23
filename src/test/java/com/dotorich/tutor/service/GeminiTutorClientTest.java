package com.dotorich.tutor.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;

import org.junit.jupiter.api.Test;

class GeminiTutorClientTest {

	@Test
	void 응답_대기_시간_초과는_다시_시도하지_않는다() {
		assertTrue(GeminiTutorClient.isResponseTimeout(new HttpTimeoutException("request timed out")));
		assertTrue(GeminiTutorClient.isResponseTimeout(new SocketTimeoutException("Read timed out")));
	}

	@Test
	void 연결_시간_초과나_연결_끊김은_다시_시도한다() {
		assertFalse(GeminiTutorClient.isResponseTimeout(new HttpConnectTimeoutException("connect timed out")));
		assertFalse(GeminiTutorClient.isResponseTimeout(new IOException("connection reset")));
	}
}
