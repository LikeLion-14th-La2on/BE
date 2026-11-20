package com.dotorich.leveltest.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** answer: OX 는 "O"/"X", 객관식·상황형은 보기 번호("0"부터), 잘 모르겠어요는 "UNKNOWN" */
public record LevelTestSubmitRequest(
	@NotNull(message = "답안을 보내 주세요")
	List<@Valid Answer> answers
) {

	public record Answer(
		@NotNull Long questionId,
		@NotBlank String answer
	) {
	}
}
