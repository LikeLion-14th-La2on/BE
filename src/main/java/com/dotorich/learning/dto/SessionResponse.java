package com.dotorich.learning.dto;

import java.util.List;

import com.dotorich.question.domain.QuestionType;
import com.fasterxml.jackson.annotation.JsonRawValue;

/** 세션 문제 조회 응답: 용어 미리보기 카드 + 문제. 정답·해설은 답안 제출 때 내려준다. */
public record SessionResponse(
		Long stageId,
		List<TermCard> terms,
		List<QuestionItem> questions
) {

	public record TermCard(
			Long termId,
			String name,
			String easyExplanation,
			String example
	) {
	}

	/**
	 * choices 는 DB 의 JSON 을 그대로 내보낸다.
	 * OX 는 null, 객관식·상황형은 ["보기1", ...], 매칭은 {"terms": [...], "meanings": [...]}
	 */
	public record QuestionItem(
			Long questionId,
			QuestionType type,
			String content,
			@JsonRawValue String choices,
			List<Long> termIds
	) {
	}
}
