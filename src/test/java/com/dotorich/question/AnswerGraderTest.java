package com.dotorich.question;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.domain.QuestionType;
import com.dotorich.question.service.AnswerGrader;

class AnswerGraderTest {

	private static Question q(QuestionType type, String choices, String answer) {
		return new Question(1L, 1L, QuestionPurpose.LEVEL_TEST, type, "문제", choices, answer, "해설", null);
	}

	@Test
	void OX는_O_X로_받아서_true_false_정답과_비교한다() {
		Question ox = q(QuestionType.OX, null, "true");
		assertTrue(AnswerGrader.isCorrect(ox, "O"));
		assertTrue(AnswerGrader.isCorrect(ox, "o"));
		assertFalse(AnswerGrader.isCorrect(ox, "X"));
		assertEquals("O", AnswerGrader.displayAnswer(ox));
		assertEquals("X", AnswerGrader.displayAnswer(q(QuestionType.OX, null, "false")));
	}

	@Test
	void 객관식은_보기_번호로_비교한다() {
		Question multiple = q(QuestionType.MULTIPLE, "[\"a\",\"b\",\"c\"]", "2");
		assertTrue(AnswerGrader.isCorrect(multiple, "2"));
		assertTrue(AnswerGrader.isCorrect(multiple, " 2 "));
		assertFalse(AnswerGrader.isCorrect(multiple, "1"));
		assertEquals("2", AnswerGrader.displayAnswer(multiple));
	}

	@Test
	void 잘_모르겠어요와_빈_답은_오답() {
		Question ox = q(QuestionType.OX, null, "true");
		assertFalse(AnswerGrader.isCorrect(ox, AnswerGrader.UNKNOWN));
		assertFalse(AnswerGrader.isCorrect(ox, null));
	}
}
