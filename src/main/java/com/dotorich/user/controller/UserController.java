package com.dotorich.user.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.user.dto.MeResponse;
import com.dotorich.user.dto.NicknameCheckResponse;
import com.dotorich.user.dto.NicknameRequest;
import com.dotorich.user.dto.NicknameResponse;
import com.dotorich.user.dto.TermsRequest;
import com.dotorich.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "사용자 (Part 1)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "내 정보·온보딩 상태 조회")
	@GetMapping("/me")
	public ApiResponse<MeResponse> me(@LoginUser Long userId) {
		return ApiResponse.ok(userService.getMe(userId));
	}

	@Operation(summary = "약관 동의")
	@PostMapping("/me/terms")
	public ApiResponse<Void> agreeTerms(@LoginUser Long userId, @Valid @RequestBody TermsRequest request) {
		userService.agreeTerms(userId, request);
		return ApiResponse.ok();
	}

	@Operation(summary = "닉네임 중복 확인 (규칙에 안 맞아도 available=false)")
	@GetMapping("/check-nickname")
	public ApiResponse<NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
		return ApiResponse.ok(userService.checkNickname(nickname));
	}

	@Operation(summary = "닉네임 등록 (온보딩)")
	@PostMapping("/me/nickname")
	public ApiResponse<NicknameResponse> registerNickname(@LoginUser Long userId,
		@Valid @RequestBody NicknameRequest request) {
		return ApiResponse.ok(userService.registerNickname(userId, request.nickname()));
	}

	@Operation(summary = "회원탈퇴")
	@DeleteMapping("/me")
	public ApiResponse<Void> withdraw(@LoginUser Long userId) {
		userService.withdraw(userId);
		return ApiResponse.ok();
	}
}
