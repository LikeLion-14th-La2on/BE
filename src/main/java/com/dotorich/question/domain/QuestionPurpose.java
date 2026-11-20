package com.dotorich.question.domain;

/** 문제 용도. 같은 questions 테이블을 학습·레벨 테스트·시사 퀴즈가 함께 쓴다. */
public enum QuestionPurpose {
	LEARNING,
	LEVEL_TEST,
	NEWS
}
