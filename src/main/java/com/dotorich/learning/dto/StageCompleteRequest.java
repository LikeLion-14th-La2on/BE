package com.dotorich.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 단계 완수 요청. correctCount: 첫 풀이 기준 맞힌 개수 (결과 요약 정답률용) */
public record StageCompleteRequest(
		@NotNull(message = "correctCount 를 보내 주세요")
		@Min(0) @Max(5)
		Integer correctCount
) {
}
