package com.dotorich.learning.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.question.domain.Question;
import com.dotorich.question.service.AnswerGrader;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * 학습 답안 채점 (P2).
 * OX·객관식·상황형은 Part 3 AnswerGrader 를 그대로 써서 레벨 테스트와 기준을 같게 하고,
 * 레벨 테스트에 없는 매칭만 여기서 채점한다.
 * 답 형식이 문제 유형과 맞지 않으면 INVALID_INPUT(400).
 */
@Component
@RequiredArgsConstructor
public class LearningAnswerGrader {

	private static final Set<String> OX = Set.of("O", "X");

	private final ObjectMapper objectMapper;

	public Result grade(Question question, Object answer) {
		return switch (question.getType()) {
			case OX -> gradeOx(question, answer);
			case MULTIPLE, SITUATION -> gradeChoice(question, answer);
			case MATCHING -> gradeMatching(question, answer);
		};
	}

	/** OX: "O" 또는 "X" (소문자도 허용) */
	private Result gradeOx(Question question, Object answer) {
		if (!(answer instanceof String text) || !OX.contains(text.trim().toUpperCase())) {
			throw invalid();
		}
		String myAnswer = text.trim().toUpperCase();
		return new Result(AnswerGrader.isCorrect(question, myAnswer), myAnswer, AnswerGrader.displayAnswer(question));
	}

	/** 객관식·상황형: "0"부터 보기 번호, 보기 개수 안이어야 한다 */
	private Result gradeChoice(Question question, Object answer) {
		if (!(answer instanceof String text)) {
			throw invalid();
		}
		int index;
		try {
			index = Integer.parseInt(text.trim());
		} catch (NumberFormatException e) {
			throw invalid();
		}
		int choiceCount = read(question.getChoices(), List.class).size();
		if (index < 0 || index >= choiceCount) {
			throw invalid();
		}
		String myAnswer = String.valueOf(index); // "01" 같은 값도 "1" 로 맞춘다
		return new Result(AnswerGrader.isCorrect(question, myAnswer), myAnswer, AnswerGrader.displayAnswer(question));
	}

	/** 매칭: {"용어": "뜻"} 이 정답과 모두 같아야 정답 (순서 무관) */
	private Result gradeMatching(Question question, Object answer) {
		if (!(answer instanceof Map<?, ?> myMap)) {
			throw invalid();
		}
		Map<?, ?> correctMap = read(question.getAnswer(), Map.class);
		return new Result(correctMap.equals(myMap), objectMapper.writeValueAsString(myMap), correctMap);
	}

	/** DB 의 JSON 글자를 읽는다. 읽을 수 없으면 시드 데이터 문제라 500 */
	private <T> T read(String json, Class<T> type) {
		if (json == null) {
			throw new IllegalStateException("문제 데이터에 보기·정답이 없습니다");
		}
		try {
			return objectMapper.readValue(json, type);
		} catch (JacksonException e) {
			throw new IllegalStateException("문제 데이터 JSON 형식 오류", e);
		}
	}

	private static BusinessException invalid() {
		return new BusinessException(ErrorCode.INVALID_INPUT);
	}

	/**
	 * correct: 정답 여부
	 * myAnswer: wrong_answers.my_answer 에 저장할 글자 ("O", "1", {"용어":"뜻"})
	 * correctAnswer: 응답에 내려줄 정답 (answer 와 같은 형식)
	 */
	public record Result(boolean correct, String myAnswer, Object correctAnswer) {
	}
}
