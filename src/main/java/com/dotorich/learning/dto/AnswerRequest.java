package com.dotorich.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 답안 제출 요청.
 * answer: OX "O"/"X", 객관식·상황형 "0"부터 보기 번호, 매칭 {"용어": "뜻"}
 * mode: LEARNING / RETRY / CARD_REVIEW
 */
public record AnswerRequest(
		@NotNull(message = "답을 보내 주세요") Object answer,
		@NotBlank(message = "mode 를 보내 주세요") String mode
) {
}
