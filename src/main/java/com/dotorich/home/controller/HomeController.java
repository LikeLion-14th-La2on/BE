package com.dotorich.home.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.home.dto.HomeResponse;
import com.dotorich.home.service.HomeService;
import com.dotorich.home.service.LearningStatusReader;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "홈 (Part 1)")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HomeController {

	private final HomeService homeService;

	@Operation(summary = "홈 조회 (Part 2 연동 전까지 학습 값은 빈 값)")
	@GetMapping("/home")
	public ApiResponse<HomeResponse> home(@LoginUser Long userId) {
		return ApiResponse.ok(homeService.getHome(userId));
	}

	@Operation(summary = "학습 현황 조회 (Part 2 연동 전까지 빈 값)")
	@GetMapping("/users/me/stats")
	public ApiResponse<LearningStatusReader.Stats> stats(@LoginUser Long userId) {
		return ApiResponse.ok(homeService.getStats(userId));
	}
}
