package com.dotorich.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드. code 는 API 명세서대로 E + HTTP 상태 (예: E404).
 * 같은 상태라도 message 로 구분하고, 파트별 구역에 추가한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "E400", "잘못된 요청입니다"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401", "로그인이 필요합니다"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "E403", "권한이 없습니다"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "대상을 찾을 수 없습니다"),
	CONFLICT(HttpStatus.CONFLICT, "E409", "이미 처리된 요청입니다"),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500", "서버 오류가 발생했습니다"),

	// Part 1 (원종윤): auth, user, home, reward
	KAKAO_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "E401", "카카오 인증에 실패했습니다"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "E401", "다시 로그인해 주세요"),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "사용자를 찾을 수 없습니다"),
	TERMS_REQUIRED(HttpStatus.BAD_REQUEST, "E400", "필수 약관에 모두 동의해 주세요"),
	NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "E409", "이미 사용 중인 닉네임입니다"),
	NICKNAME_ALREADY_SET(HttpStatus.CONFLICT, "E409", "이미 닉네임을 등록했습니다"),
	ACORN_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "E400", "도토리가 부족합니다"),
	CHARACTER_NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "캐릭터를 찾을 수 없습니다"),
	CHARACTER_ALREADY_OWNED(HttpStatus.CONFLICT, "E409", "이미 보유한 캐릭터입니다"),
	CHARACTER_NOT_OWNED(HttpStatus.FORBIDDEN, "E403", "보유하지 않은 캐릭터입니다"),
	LEVEL_TEST_ALREADY_DONE(HttpStatus.CONFLICT, "E409", "이미 레벨 테스트를 완료했습니다"),

	// Part 2 (정예원): learning, streak, review
	STAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "단계를 찾을 수 없습니다"),

	// Part 3 (김민지): leveltest, question, term, tutor, newsquiz
	TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "아직 준비되지 않은 용어입니다"),
	TUTOR_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "E503", "AI 튜터가 잠시 쉬고 있어요. 잠시 후 다시 물어봐 주세요"),

	// 각 파트는 위 구역에 "이름(...)," 형태(끝에 쉼표)로 추가한다. 세미콜론은 이 줄에만 둔다.
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}
