package com.dotorich.tutor.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.tutor.dto.TutorRequest;
import com.dotorich.tutor.dto.TutorResponse;
import com.dotorich.tutor.service.TutorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "AI 튜터 (Part 3)")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TutorController {

	private final TutorService tutorService;

	@Operation(summary = "AI 튜터 질문 (하루 무료 횟수 초과 시 도토리 차감)")
	@PostMapping("/terms/{termId}/tutor")
	public ApiResponse<TutorResponse> ask(@LoginUser Long userId, @PathVariable Long termId,
		@Valid @RequestBody TutorRequest request) {
		return ApiResponse.ok(tutorService.ask(userId, termId, request.question()));
	}
}
