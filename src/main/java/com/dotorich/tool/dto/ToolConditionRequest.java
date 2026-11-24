package com.dotorich.tool.dto;

import java.util.Map;

import com.dotorich.tool.domain.ToolType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** params: 도구별 입력값 (예: COMPOUND 는 principal, deposit, rate, years). 필드 이름은 검사하지 않는다 */
public record ToolConditionRequest(
	@NotNull(message = "toolType 을 보내 주세요") ToolType toolType,
	@NotEmpty(message = "params 를 보내 주세요") Map<String, Object> params
) {
}
