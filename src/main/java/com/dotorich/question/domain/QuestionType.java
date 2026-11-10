package com.dotorich.question.domain;

/**
 * 문제 유형. choices / answer JSON 모양
 * OX: choices 없음, answer true/false
 * MULTIPLE, SITUATION: choices ["보기1", ...], answer 정답 인덱스(0부터)
 * MATCHING: choices {"terms": [...], "meanings": [...]}, answer {"용어": "뜻", ...}
 */
public enum QuestionType {
	OX,
	MULTIPLE,
	MATCHING,
	SITUATION
}
