package com.dotorich.review.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionTerm;
import com.dotorich.question.repository.QuestionRepository;
import com.dotorich.question.repository.QuestionTermRepository;
import com.dotorich.review.domain.WrongAnswer;
import com.dotorich.review.domain.WrongAnswerStatus;
import com.dotorich.review.dto.CardReviewResponse;
import com.dotorich.review.dto.CardReviewResponse.Card;
import com.dotorich.review.dto.CardReviewResponse.QuestionItem;
import com.dotorich.review.dto.CardReviewResponse.TermCard;
import com.dotorich.review.repository.WrongAnswerRepository;
import com.dotorich.term.domain.Term;
import com.dotorich.term.repository.TermRepository;

import lombok.RequiredArgsConstructor;

/**
 * 카드뉴스 복습 조회 (P2).
 * 복습 대기(CARD_REVIEW) 문제를 전부 wrong_at 오래된 순으로 보여준다.
 * 카드에는 용어 설명과 문제만 넣고 해설은 넣지 않는다 (답을 보고 푸는 것을 막기 위해).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardReviewService {

	private final WrongAnswerRepository wrongAnswerRepository;
	private final QuestionRepository questionRepository;
	private final QuestionTermRepository questionTermRepository;
	private final TermRepository termRepository;

	public CardReviewResponse getCards(Long userId) {
		List<WrongAnswer> wrongs = wrongAnswerRepository
				.findAllByUserIdAndStatusOrderByWrongAtAsc(userId, WrongAnswerStatus.CARD_REVIEW);
		if (wrongs.isEmpty()) {
			return new CardReviewResponse(List.of());
		}

		// 문제 → 연결 용어 id → 용어 를 한 번씩만 조회해서 이어 붙인다
		List<Long> questionIds = wrongs.stream().map(WrongAnswer::getQuestionId).toList();
		Map<Long, Question> questionById = questionRepository.findAllById(questionIds).stream()
				.collect(Collectors.toMap(Question::getId, Function.identity()));
		Map<Long, List<Long>> termIdsByQuestionId = questionTermRepository.findAllByQuestionIdIn(questionIds).stream()
				.collect(Collectors.groupingBy(QuestionTerm::getQuestionId,
						Collectors.mapping(QuestionTerm::getTermId, Collectors.toList())));
		List<Long> termIds = termIdsByQuestionId.values().stream()
				.flatMap(List::stream)
				.distinct()
				.toList();
		Map<Long, Term> termById = termRepository.findAllById(termIds).stream()
				.collect(Collectors.toMap(Term::getId, Function.identity()));

		List<Card> cards = wrongs.stream()
				.map(WrongAnswer::getQuestionId)
				.filter(questionById::containsKey)
				.map(questionId -> toCard(questionById.get(questionId),
						termIdsByQuestionId.getOrDefault(questionId, List.of()), termById))
				.toList();

		return new CardReviewResponse(cards);
	}

	/** 용어는 id 순 (첫 번째 용어가 카드 제목) */
	private Card toCard(Question question, List<Long> questionTermIds, Map<Long, Term> termById) {
		List<Long> sortedTermIds = questionTermIds.stream().sorted().toList();
		List<TermCard> terms = sortedTermIds.stream()
				.map(termById::get)
				.filter(Objects::nonNull)
				.map(term -> new TermCard(term.getId(), term.getName(), term.getEasyExplanation(), term.getExample()))
				.toList();
		QuestionItem item = new QuestionItem(question.getType(), question.getContent(), question.getChoices(),
				sortedTermIds);
		return new Card(question.getId(), terms, item);
	}
}
