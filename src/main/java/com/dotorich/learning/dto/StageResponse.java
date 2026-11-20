package com.dotorich.learning.dto;

import com.dotorich.learning.domain.StageStatus;

/** 섹터별 단계 목록의 한 줄 */
public record StageResponse(
		Long stageId,
		int stepNo,
		String title,
		StageStatus status
) {
}
