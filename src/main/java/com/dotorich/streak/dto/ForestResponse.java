package com.dotorich.streak.dto;

import java.time.LocalDate;
import java.util.List;

/** 내 숲 조회 응답 (완성한 나무 도감). treeCount 는 홈 응답의 treeCount 와 같은 값 */
public record ForestResponse(
		int treeCount,
		List<TreeItem> trees
) {

	/**
	 * completedAt: 완성한 날짜 (한국 날짜, yyyy-MM-dd)
	 * fallenCount: 그 나무를 키우는 동안 끊김으로 떨어진 도토리 수
	 */
	public record TreeItem(
			Long treeId,
			LocalDate completedAt,
			int fallenCount
	) {
	}
}
