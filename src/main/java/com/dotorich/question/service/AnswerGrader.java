package com.dotorich.question.service;

import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionType;

/**
 * 채점 (P3). 레벨 테스트에서 쓰고, 학습 답안 채점(Part 2)도 같이 쓸 수 있다.
 *
 * 사용자 답(API 로 받는 값)
 * - OX: "O" / "X"
 * - MULTIPLE, SITUATION: 고른 보기 번호 "0" 부터
 * - "UNKNOWN": '잘 모르겠어요' → 항상 오답
 *
 * DB 정답(questions.answer JSON): OX 는 true/false, 객관식·상황형은 숫자.
 * MATCHING 은 보기 목록 + '잘 모르겠어요' 화면에 안 맞아 레벨 테스트에 쓰지 않는다 (여기서는 오답 처리).
 */
public final class AnswerGrader {

	public static final String UNKNOWN = "UNKNOWN";

	private AnswerGrader() {
	}

	public static boolean isCorrect(Question question, String userAnswer) {
		if (userAnswer == null || UNKNOWN.equals(userAnswer.trim())) {
			return false;
		}
		String user = userAnswer.trim();
		String stored = question.getAnswer().trim();
		QuestionType type = question.getType();
		if (type == QuestionType.OX) {
			return toOx(stored).equals(user.toUpperCase());
		}
		if (type == QuestionType.MULTIPLE || type == QuestionType.SITUATION) {
			return user.equals(stripQuotes(stored));
		}
		return false;
	}

	/** 결과 화면에 보여 줄 정답 (OX 는 "O"/"X", 객관식·상황형은 보기 번호) */
	public static String displayAnswer(Question question) {
		String stored = question.getAnswer().trim();
		if (question.getType() == QuestionType.OX) {
			return toOx(stored);
		}
		return stripQuotes(stored);
	}

	private static String toOx(String stored) {
		String value = stripQuotes(stored);
		return "true".equalsIgnoreCase(value) || "O".equalsIgnoreCase(value) ? "O" : "X";
	}

	private static String stripQuotes(String value) {
		if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1).trim();
		}
		return value;
	}
}
