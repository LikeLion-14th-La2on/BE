package com.dotorich.learning.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.learning.dto.QuestionReportRequest;
import com.dotorich.learning.service.QuestionReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "학습 (Part 2)")
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionReportController {

	private final QuestionReportService reportService;

	@Operation(summary = "문제 신고 (오타·오류, 내용 1~300자)")
	@PostMapping("/{questionId}/reports")
	public ApiResponse<Void> report(@LoginUser Long userId, @PathVariable Long questionId,
	                                @Valid @RequestBody QuestionReportRequest request) {
		reportService.report(userId, questionId, request.content());
		return ApiResponse.ok();
	}
}
