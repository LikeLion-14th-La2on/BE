package com.dotorich.review.domain;

/**
 * 오답 상태.
 * CARD_REVIEW: 재출제에서도 틀려 다음 학습 전 카드뉴스 복습 대기
 * IN_NOTE: 카드뉴스 복습에서도 틀려 오답노트에 있음
 * RESOLVED: 카드뉴스 복습에서 맞힘
 */
public enum WrongAnswerStatus {
	CARD_REVIEW,
	IN_NOTE,
	RESOLVED
}
