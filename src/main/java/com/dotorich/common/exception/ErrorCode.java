package com.dotorich.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드. 파트별 구역에 추가한다.
 * 형식: E + HTTP 상태 + 두 자리 순번 (예: E40401)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "E400", "잘못된 요청입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401", "로그인이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "E403", "권한이 없습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "대상을 찾을 수 없습니다."),
	CONFLICT(HttpStatus.CONFLICT, "E409", "이미 처리된 요청입니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500", "서버 오류가 발생했습니다.");

	// Part 1 (원종윤): auth, user, home, reward

	// Part 2 (정예원): learning, streak, review

	// Part 3 (김민지): leveltest, question, term, tutor, newsquiz

	private final HttpStatus status;
	private final String code;
	private final String message;
}
