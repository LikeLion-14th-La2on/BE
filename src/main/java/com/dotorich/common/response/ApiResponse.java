package com.dotorich.common.response;

import java.time.OffsetDateTime;
import java.util.List;

import com.dotorich.common.exception.ErrorCode;

/**
 * 공통 응답. API 명세서 규칙: { code, message, data, errors, timestamp }
 * 성공: ApiResponse.ok(data) / 실패: GlobalExceptionHandler 가 만든다.
 * timestamp 는 서버 기본 시간대(KST) 기준 +09:00 으로 나간다.
 */
public record ApiResponse<T>(
	String code,
	String message,
	T data,
	List<FieldError> errors,
	OffsetDateTime timestamp
) {

	public record FieldError(String field, String reason) {
	}

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>("S200", "Success", data, null, OffsetDateTime.now());
	}

	public static ApiResponse<Void> ok() {
		return ok(null);
	}

	public static ApiResponse<Void> fail(ErrorCode errorCode, List<FieldError> errors) {
		return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null, errors, OffsetDateTime.now());
	}
}
