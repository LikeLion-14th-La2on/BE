package com.dotorich.common.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dotorich.common.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
		ErrorCode code = e.getErrorCode();
		return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code, null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
		List<ApiResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
			.map(fe -> new ApiResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
			.toList();
		ErrorCode code = ErrorCode.INVALID_INPUT;
		return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code, errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
		log.error("처리되지 않은 예외", e);
		ErrorCode code = ErrorCode.INTERNAL_ERROR;
		return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code, null));
	}
}
