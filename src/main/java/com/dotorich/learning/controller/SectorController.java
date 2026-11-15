package com.dotorich.learning.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.learning.dto.SectorListResponse;
import com.dotorich.learning.dto.StageResponse;
import com.dotorich.learning.service.SectorService;
import com.dotorich.learning.service.StageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "학습 (Part 2)")
@RestController
@RequestMapping("/api/v1/sectors")
@RequiredArgsConstructor
public class SectorController {

	private final SectorService sectorService;
	private final StageService stageService;

	@Operation(summary = "섹터 목록 조회 (오늘의 섹터, 오늘 학습 완료 여부)")
	@GetMapping
	public ApiResponse<SectorListResponse> getSectors(@LoginUser Long userId) {
		return ApiResponse.ok(sectorService.getSectors(userId));
	}

	@Operation(summary = "섹터별 단계 목록 조회 (잠금 상태)")
	@GetMapping("/{sectorId}/stages")
	public ApiResponse<List<StageResponse>> getStages(@LoginUser Long userId, @PathVariable Long sectorId) {
		return ApiResponse.ok(stageService.getStages(userId, sectorId));
	}
}
