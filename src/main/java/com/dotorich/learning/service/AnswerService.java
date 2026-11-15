package com.dotorich.learning.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.learning.domain.AnswerMode;
import com.dotorich.learning.dto.AnswerRequest;
import com.dotorich.learning.dto.AnswerResponse;
import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.repository.QuestionRepository;
import com.dotorich.review.domain.WrongAnswer;
import com.dotorich.review.domain.WrongAnswerStatus;
import com.dotorich.review.repository.WrongAnswerRepository;

import lombok.RequiredArgsConstructor;

/**
 * 답안 제출 (P2). 학습 첫 풀이, 세션 끝 재출제, 카드뉴스 복습 공용.
 * - LEARNING: 채점만 (재출제는 프론트가 처리)
 * - RETRY 에서 틀림: 카드뉴스 복습 대기로 즉시 저장 (단계를 끝까지 안 풀고 나가도 남는다)
 * - CARD_REVIEW: 맞히면 RESOLVED, 틀리면 오답노트(IN_NOTE)
 */
@Service
@RequiredArgsConstructor
public class AnswerService {

	private final QuestionRepository questionRepository;
	private final WrongAnswerRepository wrongAnswerRepository;
	private final LearningAnswerGrader grader;

	@Transactional
	public AnswerResponse submit(Long userId, Long questionId, AnswerRequest request) {
		AnswerMode mode = AnswerMode.from(request.mode());

		// 학습 문제만 받는다 (레벨 테스트·시사 퀴즈 문제의 정답이 새지 않도록)
		Question question = questionRepository.findById(questionId)
				.filter(q -> q.getPurpose() == QuestionPurpose.LEARNING)
				.orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

		LearningAnswerGrader.Result result = grader.grade(question, request.answer());
		LocalDateTime now = LocalDateTime.now(KstTime.ZONE);

		switch (mode) {
			case LEARNING -> {
			}
			case RETRY -> {
				if (!result.correct()) {
					saveRetryWrong(userId, questionId, result.myAnswer(), now);
				}
			}
			case CARD_REVIEW -> updateCardReview(userId, questionId, result, now);
		}

		return new AnswerResponse(result.correct(), result.correctAnswer(),
				question.getExplanation(), question.getReference());
	}

	/** 재출제에서 틀림: 없으면 새로 만들고, 있으면 그 줄을 고친다 (같은 문제 중복 방지) */
	private void saveRetryWrong(Long userId, Long questionId, String myAnswer, LocalDateTime now) {
		wrongAnswerRepository.findByUserIdAndQuestionId(userId, questionId)
				.ifPresentOrElse(
						existing -> existing.wrongAgainInRetry(myAnswer, now),
						() -> wrongAnswerRepository.save(WrongAnswer.forCardReview(userId, questionId, myAnswer, now)));
	}

	/** 카드뉴스 복습: 복습 대기 중인 문제만 바꾼다 */
	private void updateCardReview(Long userId, Long questionId, LearningAnswerGrader.Result result,
	                              LocalDateTime now) {
		wrongAnswerRepository.findByUserIdAndQuestionId(userId, questionId)
				.filter(wrong -> wrong.getStatus() == WrongAnswerStatus.CARD_REVIEW)
				.ifPresent(wrong -> {
					if (result.correct()) {
						wrong.resolve();
					} else {
						wrong.moveToNote(result.myAnswer(), now);
					}
				});
	}
}
