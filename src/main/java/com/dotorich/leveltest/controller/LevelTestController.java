package com.dotorich.leveltest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.leveltest.dto.LevelTestQuestionsResponse;
import com.dotorich.leveltest.dto.LevelTestResultResponse;
import com.dotorich.leveltest.dto.LevelTestSubmitRequest;
import com.dotorich.leveltest.service.LevelTestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "레벨 테스트 (Part 3)")
@RestController
@RequestMapping("/api/v1/level-test")
@RequiredArgsConstructor
public class LevelTestController {

	private final LevelTestService levelTestService;

	@Operation(summary = "레벨 테스트 문제 조회 (섹터당 단계별 2문제, 정답 없음)")
	@GetMapping("/questions")
	public ApiResponse<LevelTestQuestionsResponse> questions(@LoginUser Long userId) {
		return ApiResponse.ok(levelTestService.getQuestions());
	}

	@Operation(summary = "레벨 테스트 결과 제출 (섹터별 시작 단계 배정, 해설)")
	@PostMapping("/result")
	public ApiResponse<LevelTestResultResponse> submit(@LoginUser Long userId,
		@Valid @RequestBody LevelTestSubmitRequest request) {
		return ApiResponse.ok(levelTestService.submit(userId, request));
	}

	@Operation(summary = "레벨 테스트 건너뛰기 (모든 섹터 1단계부터)")
	@PostMapping("/skip")
	public ApiResponse<Void> skip(@LoginUser Long userId) {
		levelTestService.skip(userId);
		return ApiResponse.ok();
	}
}
