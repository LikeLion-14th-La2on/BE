package com.dotorich.tutor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TutorRequest(
	@NotBlank(message = "질문을 입력해 주세요")
	@Size(max = 300, message = "질문은 300자 이하로 입력해 주세요")
	String question
) {
}
