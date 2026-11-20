package com.dotorich.learning.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.learning.dto.SessionResponse;
import com.dotorich.learning.dto.StageCompleteRequest;
import com.dotorich.learning.dto.StageCompleteResponse;
import com.dotorich.learning.service.SessionService;
import com.dotorich.learning.service.StageCompleteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "학습 (Part 2)")
@RestController
@RequestMapping("/api/v1/stages")
@RequiredArgsConstructor
public class StageController {

	private final SessionService sessionService;
	private final StageCompleteService stageCompleteService;

	@Operation(summary = "세션 문제 조회 (5문제 + 용어 카드)")
	@GetMapping("/{stageId}/session")
	public ApiResponse<SessionResponse> getSession(@LoginUser Long userId, @PathVariable Long stageId) {
		return ApiResponse.ok(sessionService.getSession(userId, stageId));
	}

	@Operation(summary = "단계 완수 (잠금 해제, 하루 학습 보상: 나무·도토리·스트릭)")
	@PostMapping("/{stageId}/complete")
	public ApiResponse<StageCompleteResponse> complete(@LoginUser Long userId, @PathVariable Long stageId,
	                                                   @Valid @RequestBody StageCompleteRequest request) {
		return ApiResponse.ok(stageCompleteService.complete(userId, stageId));
	}
}
