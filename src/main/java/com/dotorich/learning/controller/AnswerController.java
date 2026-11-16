package com.dotorich.learning.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.learning.dto.AnswerRequest;
import com.dotorich.learning.dto.AnswerResponse;
import com.dotorich.learning.service.AnswerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "학습 (Part 2)")
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class AnswerController {

	private final AnswerService answerService;

	@Operation(summary = "답안 제출 (채점 + 해설, 학습·재출제·카드뉴스 복습 공용)")
	@PostMapping("/{questionId}/answer")
	public ApiResponse<AnswerResponse> submit(@LoginUser Long userId, @PathVariable Long questionId,
	                                          @Valid @RequestBody AnswerRequest request) {
		return ApiResponse.ok(answerService.submit(userId, questionId, request));
	}
}
