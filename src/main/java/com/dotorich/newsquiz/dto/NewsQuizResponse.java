package com.dotorich.newsquiz.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonRawValue;

/** 오늘의 시사 퀴즈. 정답은 내려주지 않는다. */
public record NewsQuizResponse(Long quizId, LocalDate publishDate, QuestionItem question) {

	/** choices 는 DB 의 JSON 배열을 그대로 내보낸다 (예: ["보기1", "보기2"]), OX 는 null */
	public record QuestionItem(String type, String content, @JsonRawValue String choices) {
	}
}
