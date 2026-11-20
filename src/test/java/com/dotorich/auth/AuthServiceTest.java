package com.dotorich.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.dotorich.auth.domain.RefreshToken;
import com.dotorich.auth.repository.RefreshTokenRepository;
import com.dotorich.auth.service.AuthService;
import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.jwt.JwtProvider;

class AuthServiceTest {

	@Test
	void 이미_지워진_토큰으로_재발급하면_INVALID_REFRESH_TOKEN() {
		JwtProvider jwtProvider = mock(JwtProvider.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		AuthService authService = new AuthService(null, jwtProvider, null, refreshTokenRepository, null);

		when(jwtProvider.parseRefreshToken("rt")).thenReturn(1L);
		when(refreshTokenRepository.findByToken("rt"))
			.thenReturn(Optional.of(new RefreshToken(1L, "rt", LocalDateTime.now().plusDays(1))));
		// 다른 요청이 먼저 지운 상황
		when(refreshTokenRepository.deleteByToken("rt")).thenReturn(0);

		BusinessException e = assertThrows(BusinessException.class, () -> authService.refresh("rt"));
		assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, e.getErrorCode());
		verify(refreshTokenRepository, never()).save(any());
	}
}
