package com.dotorich.streak.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.streak.dto.ForestResponse;
import com.dotorich.streak.service.ForestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "스트릭·나무 (Part 2)")
@RestController
@RequestMapping("/api/v1/forest")
@RequiredArgsConstructor
public class ForestController {

	private final ForestService forestService;

	@Operation(summary = "내 숲 조회 (완성한 나무 도감, 최신순)")
	@GetMapping
	public ApiResponse<ForestResponse> getForest(@LoginUser Long userId) {
		return ApiResponse.ok(forestService.getForest(userId));
	}
}
