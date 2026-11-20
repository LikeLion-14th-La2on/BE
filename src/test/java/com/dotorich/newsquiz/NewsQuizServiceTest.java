package com.dotorich.newsquiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.newsquiz.domain.NewsQuiz;
import com.dotorich.newsquiz.dto.NewsQuizAnswerResponse;
import com.dotorich.newsquiz.dto.NewsQuizResponse;
import com.dotorich.newsquiz.repository.NewsQuizRepository;
import com.dotorich.newsquiz.service.NewsQuizService;
import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.domain.QuestionType;
import com.dotorich.question.repository.QuestionRepository;

class NewsQuizServiceTest {

	private NewsQuizRepository newsQuizRepository;
	private QuestionRepository questionRepository;
	private NewsQuizService newsQuizService;

	private final LocalDate today = LocalDate.now(KstTime.ZONE);
	private final Question question = new Question(70L, null, QuestionPurpose.NEWS, QuestionType.OX,
		"기준금리가 오르면 대출 이자도 보통 오른다.", null, "true", "기준금리가 오르면 은행 대출 금리도 따라 오르는 경우가 많아요.", null);

	@BeforeEach
	void setUp() {
		newsQuizRepository = mock(NewsQuizRepository.class);
		questionRepository = mock(QuestionRepository.class);
		newsQuizService = new NewsQuizService(newsQuizRepository, questionRepository);
		when(questionRepository.findById(70L)).thenReturn(Optional.of(question));
	}

	@Test
	void 오늘_또는_가장_최근_퀴즈를_정답_없이_조회한다() {
		when(newsQuizRepository.findFirstByPublishDateLessThanEqualOrderByPublishDateDesc(any()))
			.thenReturn(Optional.of(new NewsQuiz(3L, 70L, today.minusDays(2))));

		NewsQuizResponse response = newsQuizService.getToday();

		assertEquals(3L, response.quizId());
		assertEquals(today.minusDays(2), response.publishDate());
		assertEquals("OX", response.question().type());
		assertNull(response.question().choices());
	}

	@Test
	void 등록된_퀴즈가_없으면_404() {
		when(newsQuizRepository.findFirstByPublishDateLessThanEqualOrderByPublishDateDesc(any()))
			.thenReturn(Optional.empty());

		BusinessException e = assertThrows(BusinessException.class, () -> newsQuizService.getToday());
		assertEquals(ErrorCode.NEWS_QUIZ_NOT_FOUND, e.getErrorCode());
	}

	@Test
	void 답을_채점하고_정답과_해설을_준다() {
		when(newsQuizRepository.findById(3L)).thenReturn(Optional.of(new NewsQuiz(3L, 70L, today)));

		NewsQuizAnswerResponse correct = newsQuizService.answer(3L, "O");
		NewsQuizAnswerResponse wrong = newsQuizService.answer(3L, "X");

		assertTrue(correct.correct());
		assertEquals("O", correct.correctAnswer());
		assertFalse(wrong.correct());
		assertEquals(question.getExplanation(), wrong.explanation());
	}

	@Test
	void 게시일이_안_된_퀴즈에는_답할_수_없다() {
		when(newsQuizRepository.findById(4L)).thenReturn(Optional.of(new NewsQuiz(4L, 70L, today.plusDays(1))));

		BusinessException e = assertThrows(BusinessException.class, () -> newsQuizService.answer(4L, "O"));
		assertEquals(ErrorCode.NEWS_QUIZ_NOT_FOUND, e.getErrorCode());
	}
}
