package com.dotorich.newsquiz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.newsquiz.dto.NewsQuizAnswerRequest;
import com.dotorich.newsquiz.dto.NewsQuizAnswerResponse;
import com.dotorich.newsquiz.dto.NewsQuizResponse;
import com.dotorich.newsquiz.service.NewsQuizService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "시사 퀴즈 (Part 3)")
@RestController
@RequestMapping("/api/v1/news-quizzes")
@RequiredArgsConstructor
public class NewsQuizController {

	private final NewsQuizService newsQuizService;

	@Operation(summary = "시사 퀴즈 조회 (오늘, 없으면 가장 최근 게시분)")
	@GetMapping("/today")
	public ApiResponse<NewsQuizResponse> today(@LoginUser Long userId) {
		return ApiResponse.ok(newsQuizService.getToday());
	}

	@Operation(summary = "시사 퀴즈 답안 제출 (채점만, 오답노트·스트릭 미반영)")
	@PostMapping("/{quizId}/answer")
	public ApiResponse<NewsQuizAnswerResponse> answer(@LoginUser Long userId, @PathVariable Long quizId,
		@Valid @RequestBody NewsQuizAnswerRequest request) {
		return ApiResponse.ok(newsQuizService.answer(quizId, request.answer()));
	}
}
