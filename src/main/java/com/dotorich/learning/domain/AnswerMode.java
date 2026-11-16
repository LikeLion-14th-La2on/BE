package com.dotorich.learning.domain;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;

/** 답안 제출을 어디서 했는지: 학습 첫 풀이, 세션 끝 재출제, 카드뉴스 복습 */
public enum AnswerMode {
	LEARNING,
	RETRY,
	CARD_REVIEW;

	/** 모르는 값이면 400 */
	public static AnswerMode from(String value) {
		try {
			return AnswerMode.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}
}
