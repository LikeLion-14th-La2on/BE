package com.dotorich.review.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.review.dto.CardReviewResponse;
import com.dotorich.review.service.CardReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "복습 (Part 2)")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final CardReviewService cardReviewService;

	@Operation(summary = "카드뉴스 복습 조회 (복습 대기 문제 전부, 오래된 순, 해설 없음)")
	@GetMapping("/today")
	public ApiResponse<CardReviewResponse> getTodayCards(@LoginUser Long userId) {
		return ApiResponse.ok(cardReviewService.getCards(userId));
	}
}
