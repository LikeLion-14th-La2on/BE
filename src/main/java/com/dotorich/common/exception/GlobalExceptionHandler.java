package com.dotorich.common.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

	// 본문 없음, 깨진 JSON, 타입이 안 맞거나 빠진 파라미터
	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
		MissingServletRequestParameterException.class, MissingPathVariableException.class})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
		return clientError(ErrorCode.INVALID_INPUT, e);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
		return clientError(ErrorCode.NOT_FOUND, e);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return clientError(ErrorCode.METHOD_NOT_ALLOWED, e);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
		log.error("처리되지 않은 예외", e);
		ErrorCode code = ErrorCode.INTERNAL_ERROR;
		return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code, null));
	}

	// 클라이언트 잘못이므로 스택트레이스 없이 WARN 한 줄만
	private ResponseEntity<ApiResponse<Void>> clientError(ErrorCode code, Exception e) {
		log.warn("{} {}: {}", code.getCode(), e.getClass().getSimpleName(), e.getMessage());
		return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code, null));
	}
}
