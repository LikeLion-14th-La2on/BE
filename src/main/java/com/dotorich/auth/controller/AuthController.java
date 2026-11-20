package com.dotorich.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.auth.dto.KakaoLoginRequest;
import com.dotorich.auth.dto.LoginResponse;
import com.dotorich.auth.dto.RefreshRequest;
import com.dotorich.auth.dto.TokenResponse;
import com.dotorich.auth.service.AuthService;
import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "인증 (Part 1)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "카카오 로그인 (인가 코드 → JWT)")
	@PostMapping("/kakao")
	public ApiResponse<LoginResponse> kakao(@Valid @RequestBody KakaoLoginRequest request) {
		return ApiResponse.ok(authService.kakaoLogin(request));
	}

	@Operation(summary = "토큰 재발급")
	@PostMapping("/refresh")
	public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
		return ApiResponse.ok(authService.refresh(request.refreshToken()));
	}

	@Operation(summary = "로그아웃")
	@PostMapping("/logout")
	public ApiResponse<Void> logout(@LoginUser Long userId) {
		authService.logout(userId);
		return ApiResponse.ok();
	}
}
