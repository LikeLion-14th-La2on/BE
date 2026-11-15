package com.dotorich.learning.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.learning.dto.SessionResponse;
import com.dotorich.learning.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "학습 (Part 2)")
@RestController
@RequestMapping("/api/v1/stages")
@RequiredArgsConstructor
public class StageController {

	private final SessionService sessionService;

	@Operation(summary = "세션 문제 조회 (5문제 + 용어 카드)")
	@GetMapping("/{stageId}/session")
	public ApiResponse<SessionResponse> getSession(@LoginUser Long userId, @PathVariable Long stageId) {
		return ApiResponse.ok(sessionService.getSession(userId, stageId));
	}
}
