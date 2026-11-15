package com.dotorich.learning.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 섹터 목록 조회 응답 */
public record SectorListResponse(
		boolean todayCompleted,
		List<SectorItem> sectors
) {

	public record SectorItem(
			Long sectorId,
			String name,
			@JsonProperty("isToday") boolean isToday
	) {
	}
}
