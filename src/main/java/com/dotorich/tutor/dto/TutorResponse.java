package com.dotorich.tutor.dto;

/**
 * freeRemaining: 오늘 남은 무료 횟수, acornSpent: 이번 질문에 쓴 도토리, acornBalance: 질문 후 잔액
 */
public record TutorResponse(
	String answer,
	int freeRemaining,
	int acornSpent,
	int acornBalance
) {
}
