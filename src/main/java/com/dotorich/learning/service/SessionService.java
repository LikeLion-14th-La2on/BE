package com.dotorich.learning.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.learning.domain.Sector;
import com.dotorich.learning.domain.Stage;
import com.dotorich.learning.dto.SessionResponse;
import com.dotorich.learning.dto.SessionResponse.QuestionItem;
import com.dotorich.learning.dto.SessionResponse.TermCard;
import com.dotorich.learning.repository.StageRepository;
import com.dotorich.learning.repository.UserStageProgressRepository;
import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.domain.QuestionTerm;
import com.dotorich.question.repository.QuestionRepository;
import com.dotorich.question.repository.QuestionTermRepository;
import com.dotorich.streak.repository.UserStreakRepository;
import com.dotorich.term.domain.Term;
import com.dotorich.term.repository.TermRepository;

import lombok.RequiredArgsConstructor;

/** 세션 문제 조회 (P2). 문제·용어 데이터는 Part 3 것을 읽기만 한다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

	private final StageRepository stageRepository;
	private final UserStageProgressRepository progressRepository;
	private final UserStreakRepository userStreakRepository;
	private final TodaySectorFinder todaySectorFinder;
	private final QuestionRepository questionRepository;
	private final QuestionTermRepository questionTermRepository;
	private final TermRepository termRepository;

	public SessionResponse getSession(Long userId, Long stageId) {
		Stage stage = stageRepository.findById(stageId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
		checkCanStudy(userId, stage);

		List<Question> questions = questionRepository
				.findAllByStageIdAndPurposeOrderByIdAsc(stageId, QuestionPurpose.LEARNING);
		List<Long> questionIds = questions.stream().map(Question::getId).toList();

		// 문제 id → 연결된 용어 id 목록
		Map<Long, List<Long>> termIdsByQuestionId = questionTermRepository.findAllByQuestionIdIn(questionIds).stream()
				.collect(Collectors.groupingBy(QuestionTerm::getQuestionId,
						Collectors.mapping(QuestionTerm::getTermId, Collectors.toList())));

		// 이 단계 문제에 연결된 용어를 겹치지 않게 모아 용어 카드로
		List<Long> termIds = termIdsByQuestionId.values().stream()
				.flatMap(List::stream)
				.distinct()
				.toList();
		List<TermCard> terms = termRepository.findAllById(termIds).stream()
				.sorted(Comparator.comparing(Term::getId))
				.map(term -> new TermCard(term.getId(), term.getName(), term.getEasyExplanation(), term.getExample()))
				.toList();

		List<QuestionItem> items = questions.stream()
				.map(question -> new QuestionItem(question.getId(), question.getType(), question.getContent(),
						question.getChoices(), termIdsByQuestionId.getOrDefault(question.getId(), List.of())))
				.toList();

		return new SessionResponse(stageId, terms, items);
	}

	/** 잠긴 단계, 오늘의 섹터 학습 전의 다른 섹터는 풀 수 없다 */
	private void checkCanStudy(Long userId, Stage stage) {
		if (!progressRepository.existsByUserIdAndStageId(userId, stage.getId())) {
			throw new BusinessException(ErrorCode.STAGE_LOCKED);
		}

		LocalDate today = LocalDate.now(KstTime.ZONE);
		boolean todayCompleted = userStreakRepository.findById(userId)
				.map(streak -> streak.studiedOn(today))
				.orElse(false);
		Optional<Sector> todaySector = todaySectorFinder.find(today);

		// 일요일(오늘의 섹터 없음)은 제한 없음. 일요일 규칙 확정 전까지의 처리
		boolean otherSector = todaySector.isPresent() && !todaySector.get().getId().equals(stage.getSectorId());
		if (!todayCompleted && otherSector) {
			throw new BusinessException(ErrorCode.TODAY_SECTOR_FIRST);
		}
	}
}
