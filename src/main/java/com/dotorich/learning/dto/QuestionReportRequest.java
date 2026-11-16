package com.dotorich.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 문제 신고 요청. content: 신고 내용 (1~300자) */
public record QuestionReportRequest(
		@NotBlank(message = "신고 내용을 입력해 주세요")
		@Size(max = 300, message = "신고 내용은 300자 이하로 입력해 주세요")
		String content
) {
}
