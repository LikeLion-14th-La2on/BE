package com.dotorich.leveltest.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;

/** 레벨 테스트 문제 조회. 정답은 내려주지 않는다. '잘 모르겠어요'는 FE 가 보기 끝에 붙인다. */
public record LevelTestQuestionsResponse(List<SectorQuestions> sectors) {

	public record SectorQuestions(Long sectorId, String name, List<QuestionItem> questions) {
	}

	/** choices 는 DB 의 JSON 배열을 그대로 내보낸다 (예: ["보기1", "보기2"]), OX 는 null */
	public record QuestionItem(Long questionId, int stepNo, String type, String content,
		@JsonRawValue String choices) {
	}
}
