package com.dotorich.common.response;

import java.time.LocalDateTime;
import java.util.List;

import com.dotorich.common.exception.ErrorCode;

/**
 * 공통 응답. API 명세서 규칙: { code, message, data, errors, timestamp }
 * 성공: ApiResponse.ok(data) / 실패: GlobalExceptionHandler 가 만든다.
 */
public record ApiResponse<T>(
	String code,
	String message,
	T data,
	List<FieldError> errors,
	LocalDateTime timestamp
) {

	public record FieldError(String field, String reason) {
	}

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>("S200", "요청 성공", data, null, LocalDateTime.now());
	}

	public static ApiResponse<Void> ok() {
		return ok(null);
	}

	public static ApiResponse<Void> fail(ErrorCode errorCode, List<FieldError> errors) {
		return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null, errors, LocalDateTime.now());
	}
}
