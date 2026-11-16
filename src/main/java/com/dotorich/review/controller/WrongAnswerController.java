package com.dotorich.review.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.review.dto.WrongAnswerWeekResponse;
import com.dotorich.review.service.WrongAnswerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "복습 (Part 2)")
@RestController
@RequestMapping("/api/v1/wrong-answers")
@RequiredArgsConstructor
public class WrongAnswerController {

	private final WrongAnswerService wrongAnswerService;

	@Operation(summary = "오답노트 주차별 조회 (week 예: 2026-W45, 생략 시 이번 주 / sectorId 생략 시 전체)")
	@GetMapping
	public ApiResponse<WrongAnswerWeekResponse> getWrongAnswers(
			@LoginUser Long userId,
			@RequestParam(required = false) String week,
			@RequestParam(required = false) Long sectorId) {
		return ApiResponse.ok(wrongAnswerService.getWeek(userId, week, sectorId));
	}
}
