package com.dotorich.review.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.dotorich.question.domain.QuestionType;
import com.fasterxml.jackson.annotation.JsonRawValue;

/** 오답노트 주차별 조회 응답 */
public record WrongAnswerWeekResponse(
		String week,
		String weekLabel,
		List<Item> items
) {

	/**
	 * choices 는 학습 세션 조회와 같은 형식 (OX 는 null).
	 * myAnswer·correctAnswer 는 답안 제출 형식 (OX "O"/"X", 객관식·상황형 "0"부터, 매칭 {"용어": "뜻"})
	 */
	public record Item(
			Long wrongAnswerId,
			Long questionId,
			String sectorName,
			QuestionType type,
			String content,
			@JsonRawValue String choices,
			Object myAnswer,
			Object correctAnswer,
			String explanation,
			String reference,
			OffsetDateTime wrongAt
	) {
	}
}
