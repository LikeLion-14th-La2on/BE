package com.dotorich.newsquiz.dto;

import jakarta.validation.constraints.NotBlank;

/** answer: OX 는 "O"/"X", 객관식·상황형은 보기 번호 "0" 부터, 잘 모르겠어요는 "UNKNOWN" */
public record NewsQuizAnswerRequest(
	@NotBlank(message = "답을 골라 주세요")
	String answer
) {
}
