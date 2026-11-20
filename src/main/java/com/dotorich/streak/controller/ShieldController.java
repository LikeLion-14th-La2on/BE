package com.dotorich.streak.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.streak.dto.ShieldPurchaseResponse;
import com.dotorich.streak.service.ShieldService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "스트릭·나무 (Part 2)")
@RestController
@RequestMapping("/api/v1/streak")
@RequiredArgsConstructor
public class ShieldController {

	private final ShieldService shieldService;

	@Operation(summary = "스트릭 보호권 구매 (도토리 5개, 최대 2개, 1개당 하루)")
	@PostMapping("/shields")
	public ApiResponse<ShieldPurchaseResponse> purchase(@LoginUser Long userId) {
		return ApiResponse.ok(shieldService.purchase(userId));
	}
}
