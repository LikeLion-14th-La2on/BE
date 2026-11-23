package com.dotorich.tool.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.tool.domain.ToolType;
import com.dotorich.tool.dto.ToolConditionRequest;
import com.dotorich.tool.dto.ToolConditionResponse;
import com.dotorich.tool.service.ToolConditionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "금융 도구 (Part 1)")
@RestController
@RequestMapping("/api/v1/tools/conditions")
@RequiredArgsConstructor
public class ToolConditionController {

	private final ToolConditionService toolConditionService;

	@Operation(summary = "내 조건 조회 (저장한 적 없으면 data null)")
	@GetMapping
	public ApiResponse<ToolConditionResponse> get(@LoginUser Long userId, @RequestParam ToolType toolType) {
		return ApiResponse.ok(toolConditionService.get(userId, toolType));
	}

	@Operation(summary = "내 조건 저장 (도구별 하나, 있으면 덮어씀)")
	@PutMapping
	public ApiResponse<Void> save(@LoginUser Long userId, @Valid @RequestBody ToolConditionRequest request) {
		toolConditionService.save(userId, request);
		return ApiResponse.ok();
	}
}
