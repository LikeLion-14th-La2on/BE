package com.dotorich.newsquiz.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.newsquiz.domain.NewsQuiz;
import com.dotorich.newsquiz.dto.NewsQuizAnswerResponse;
import com.dotorich.newsquiz.dto.NewsQuizResponse;
import com.dotorich.newsquiz.repository.NewsQuizRepository;
import com.dotorich.question.domain.Question;
import com.dotorich.question.repository.QuestionRepository;
import com.dotorich.question.service.AnswerGrader;

import lombok.RequiredArgsConstructor;

/**
 * 시사 퀴즈 (P3, 2순위). 팀이 주 1회 시드로 등록한다.
 * 채점만 하고 기록은 남기지 않는다 (오답노트·스트릭·도토리에 반영하지 않음).
 * 아직 게시일이 오지 않은 퀴즈는 조회·제출 모두 404.
 */
@Service
@RequiredArgsConstructor
public class NewsQuizService {

	private final NewsQuizRepository newsQuizRepository;
	private final QuestionRepository questionRepository;

	@Transactional(readOnly = true)
	public NewsQuizResponse getToday() {
		NewsQuiz quiz = newsQuizRepository.findFirstByPublishDateLessThanEqualOrderByPublishDateDesc(today())
			.orElseThrow(() -> new BusinessException(ErrorCode.NEWS_QUIZ_NOT_FOUND));
		Question question = getQuestion(quiz);
		return new NewsQuizResponse(quiz.getId(), quiz.getPublishDate(), new NewsQuizResponse.QuestionItem(
			question.getType().name(), question.getContent(), question.getChoices()));
	}

	@Transactional(readOnly = true)
	public NewsQuizAnswerResponse answer(Long quizId, String answer) {
		NewsQuiz quiz = newsQuizRepository.findById(quizId)
			.filter(q -> !q.getPublishDate().isAfter(today()))
			.orElseThrow(() -> new BusinessException(ErrorCode.NEWS_QUIZ_NOT_FOUND));
		Question question = getQuestion(quiz);
		return new NewsQuizAnswerResponse(AnswerGrader.isCorrect(question, answer),
			AnswerGrader.displayAnswer(question), question.getExplanation());
	}

	private Question getQuestion(NewsQuiz quiz) {
		return questionRepository.findById(quiz.getQuestionId())
			.orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
	}

	private static LocalDate today() {
		return LocalDate.now(KstTime.ZONE);
	}
}
