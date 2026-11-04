package com.dotorich.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.dotorich.common.jwt.JwtProvider;

class JwtProviderTest {

	private final JwtProvider jwt = new JwtProvider("test-secret-key-test-secret-key-32bytes!!");

	@Test
	void 발급한_토큰에서_userId를_꺼낸다() {
		assertEquals(7L, jwt.parseAccessToken(jwt.createAccessToken(7L)));
		assertEquals(7L, jwt.parseRefreshToken(jwt.createRefreshToken(7L)));
	}

	@Test
	void Refresh_토큰은_Access로_쓸_수_없다() {
		assertNull(jwt.parseAccessToken(jwt.createRefreshToken(7L)));
		assertNull(jwt.parseRefreshToken(jwt.createAccessToken(7L)));
	}

	@Test
	void 다른_키로_서명한_토큰은_거부() {
		JwtProvider other = new JwtProvider("other-secret-key-other-secret-key-32b!!");
		assertNull(jwt.parseAccessToken(other.createAccessToken(7L)));
		assertNull(jwt.parseAccessToken("not-a-token"));
	}
}
