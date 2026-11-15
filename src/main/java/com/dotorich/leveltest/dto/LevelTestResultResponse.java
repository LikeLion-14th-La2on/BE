package com.dotorich.leveltest.dto;

import java.util.List;

/** results: 섹터별 시작 단계, explanations: 결과 화면에 한꺼번에 보여 줄 해설 */
public record LevelTestResultResponse(
	List<SectorResult> results,
	List<Explanation> explanations
) {

	public record SectorResult(Long sectorId, String name, int startStepNo) {
	}

	public record Explanation(Long questionId, boolean correct, String correctAnswer, String explanation) {
	}
}
