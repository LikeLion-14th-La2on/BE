package com.dotorich.review.dto;

import java.util.List;

import com.dotorich.question.domain.QuestionType;
import com.fasterxml.jackson.annotation.JsonRawValue;

/** 카드뉴스 복습 조회 응답. 해설은 넣지 않는다 (답안 제출 응답에서 받는다) */
public record CardReviewResponse(
		List<Card> cards
) {

	/** 카드 1장 = 복습할 문제 1개 + 그 문제에 연결된 용어 (카드 제목은 terms 의 첫 번째 용어) */
	public record Card(
			Long questionId,
			List<TermCard> terms,
			QuestionItem question
	) {
	}

	/** 학습 세션 조회의 용어 카드와 같은 형식 */
	public record TermCard(
			Long termId,
			String name,
			String easyExplanation,
			String example
	) {
	}

	/** choices 는 학습 세션 조회와 같은 형식 (OX 는 null) */
	public record QuestionItem(
			QuestionType type,
			String content,
			@JsonRawValue String choices,
			List<Long> termIds
	) {
	}
}
