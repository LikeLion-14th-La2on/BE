package com.dotorich.auth.service;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.auth.domain.RefreshToken;
import com.dotorich.auth.dto.KakaoLoginRequest;
import com.dotorich.auth.dto.LoginResponse;
import com.dotorich.auth.dto.TokenResponse;
import com.dotorich.auth.repository.RefreshTokenRepository;
import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.jwt.JwtProvider;
import com.dotorich.reward.service.CharacterService;
import com.dotorich.user.domain.User;
import com.dotorich.user.domain.UserDeletedEvent;
import com.dotorich.user.dto.OnboardingResponse;
import com.dotorich.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final KakaoClient kakaoClient;
	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final CharacterService characterService;

	@Transactional
	public LoginResponse kakaoLogin(KakaoLoginRequest request) {
		String kakaoId = kakaoClient.getKakaoId(request.code(), request.redirectUri());

		User user = userRepository.findByKakaoId(kakaoId).orElse(null);
		boolean isNewUser = user == null;
		if (isNewUser) {
			user = userRepository.save(User.kakao(kakaoId));
			characterService.grantDefaultCharacters(user);
		}

		TokenResponse tokens = issueTokens(user.getId());
		return new LoginResponse(tokens.accessToken(), tokens.refreshToken(), isNewUser, OnboardingResponse.of(user));
	}

	@Transactional
	public TokenResponse refresh(String refreshToken) {
		Long userId = jwtProvider.parseRefreshToken(refreshToken);
		RefreshToken saved = refreshTokenRepository.findByToken(refreshToken).orElse(null);
		if (userId == null || saved == null || saved.isExpired() || !saved.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
		}
		refreshTokenRepository.delete(saved);
		return issueTokens(userId);
	}

	/** 이 사용자의 모든 기기에서 로그아웃 (Refresh Token 전부 삭제) */
	@Transactional
	public void logout(Long userId) {
		refreshTokenRepository.deleteAllByUserId(userId);
	}

	@EventListener
	public void onUserDeleted(UserDeletedEvent event) {
		refreshTokenRepository.deleteAllByUserId(event.userId());
	}

	private TokenResponse issueTokens(Long userId) {
		String access = jwtProvider.createAccessToken(userId);
		String refresh = jwtProvider.createRefreshToken(userId);
		refreshTokenRepository.save(new RefreshToken(userId, refresh,
			LocalDateTime.now().plus(JwtProvider.REFRESH_TTL)));
		return new TokenResponse(access, refresh);
	}
}
