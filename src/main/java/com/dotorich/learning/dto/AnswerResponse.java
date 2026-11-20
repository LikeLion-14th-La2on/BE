package com.dotorich.learning.dto;

/** 답안 제출 응답. correctAnswer 는 answer 와 같은 형식 */
public record AnswerResponse(
		boolean correct,
		Object correctAnswer,
		String explanation,
		String reference
) {
}
