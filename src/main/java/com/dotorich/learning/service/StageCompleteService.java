package com.dotorich.learning.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.learning.domain.Sector;
import com.dotorich.learning.domain.Stage;
import com.dotorich.learning.domain.StageStatus;
import com.dotorich.learning.domain.UserStageProgress;
import com.dotorich.learning.dto.StageCompleteResponse;
import com.dotorich.learning.dto.StageCompleteResponse.TreeInfo;
import com.dotorich.learning.repository.StageRepository;
import com.dotorich.learning.repository.UserStageProgressRepository;
import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.domain.QuestionTerm;
import com.dotorich.question.repository.QuestionRepository;
import com.dotorich.question.repository.QuestionTermRepository;
import com.dotorich.review.domain.WrongAnswerStatus;
import com.dotorich.review.repository.WrongAnswerRepository;
import com.dotorich.reward.domain.AcornReason;
import com.dotorich.reward.service.AcornService;
import com.dotorich.streak.domain.StudyDay;
import com.dotorich.streak.domain.Tree;
import com.dotorich.streak.domain.UserStreak;
import com.dotorich.streak.repository.StudyDayRepository;
import com.dotorich.streak.repository.TreeRepository;
import com.dotorich.streak.repository.UserStreakRepository;

import lombok.RequiredArgsConstructor;

/**
 * 단계 완수 (P2).
 * 1. 단계를 COMPLETED 로, 같은 섹터 다음 단계가 잠겨 있으면 UNLOCKED 로
 * 2. 하루 학습(평일: 오늘의 섹터, 일요일: 아무 섹터)이고 오늘 첫 학습이면
 *    학습일 기록, 스트릭, 나무 1칸 + 도토리 +1, 7칸이면 +5 와 새 나무
 * 3. 그 외(추가 풀이, 오늘 이미 학습)는 1번만
 */
@Service
@RequiredArgsConstructor
public class StageCompleteService {

	private final StageRepository stageRepository;
	private final UserStageProgressRepository progressRepository;
	private final TodaySectorFinder todaySectorFinder;
	private final UserStreakRepository userStreakRepository;
	private final StudyDayRepository studyDayRepository;
	private final TreeRepository treeRepository;
	private final AcornService acornService;
	private final QuestionRepository questionRepository;
	private final QuestionTermRepository questionTermRepository;
	private final WrongAnswerRepository wrongAnswerRepository;

	@Transactional
	public StageCompleteResponse complete(Long userId, Long stageId) {
		Stage stage = stageRepository.findById(stageId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
		UserStageProgress progress = progressRepository.findByUserIdAndStageId(userId, stageId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STAGE_LOCKED));

		LocalDateTime now = LocalDateTime.now(KstTime.ZONE);
		LocalDate today = now.toLocalDate();

		// 1. 단계 완수, 다음 단계 열기
		progress.complete(now);
		Long nextStageId = unlockNextStage(userId, stage);

		// 2. 하루 학습 보상
		UserStreak streak = userStreakRepository.findById(userId)
				.orElseGet(() -> userStreakRepository.save(new UserStreak(userId)));
		boolean dailyRewarded = false;
		int acornEarned = 0;
		boolean treeCompleted = false;
		Tree tree = treeRepository.findFirstByUserIdAndCompletedAtIsNull(userId).orElse(null);

		if (isDailyStudy(stage, today) && !streak.studiedOn(today)) {
			dailyRewarded = true;
			streak.recordStudy(today);
			studyDayRepository.save(new StudyDay(userId, today));

			if (acornService.earn(userId, AcornService.DAILY_STUDY_REWARD, AcornReason.DAILY_STUDY, toYyyymmdd(today))) {
				acornEarned += AcornService.DAILY_STUDY_REWARD;
			}

			if (tree == null) {
				tree = treeRepository.save(Tree.start(userId));
			}
			if (tree.grow(now)) {
				treeCompleted = true;
				if (acornService.earn(userId, AcornService.TREE_COMPLETE_BONUS, AcornReason.TREE_COMPLETE, tree.getId())) {
					acornEarned += AcornService.TREE_COMPLETE_BONUS;
				}
				tree = treeRepository.save(Tree.start(userId)); // 완성된 나무는 내 숲으로, 새 나무 시작
			}
		}

		TreeInfo treeInfo = tree == null
				? new TreeInfo(0, Tree.SIZE, 0)
				: new TreeInfo(tree.getFilledCount(), Tree.SIZE, tree.getFallenCount());

		return new StageCompleteResponse(
				dailyRewarded,
				acornEarned,
				acornService.getBalance(userId),
				streak.getCurrentCount(),
				treeInfo,
				treeCompleted,
				countStageTerms(stageId),
				wrongAnswerRepository.countByUserIdAndStatus(userId, WrongAnswerStatus.CARD_REVIEW),
				nextStageId);
	}

	/** 같은 섹터 다음 단계가 잠겨 있으면 연다. 다음 단계 id (마지막 단계면 null) */
	private Long unlockNextStage(Long userId, Stage stage) {
		Optional<Stage> next = stageRepository.findBySectorIdAndStepNo(stage.getSectorId(), stage.getStepNo() + 1);
		next.ifPresent(nextStage -> {
			if (!progressRepository.existsByUserIdAndStageId(userId, nextStage.getId())) {
				progressRepository.save(new UserStageProgress(userId, nextStage.getId(), StageStatus.UNLOCKED));
			}
		});
		return next.map(Stage::getId).orElse(null);
	}

	/** 평일은 오늘의 섹터, 일요일(오늘의 섹터 없음)은 아무 섹터 */
	private boolean isDailyStudy(Stage stage, LocalDate today) {
		Optional<Sector> todaySector = todaySectorFinder.find(today);
		return todaySector.isEmpty() || todaySector.get().getId().equals(stage.getSectorId());
	}

	/** 이번 단계 학습 문제에 연결된 용어 수 (겹치는 용어는 한 번) */
	private int countStageTerms(Long stageId) {
		List<Long> questionIds = questionRepository
				.findAllByStageIdAndPurposeOrderByIdAsc(stageId, QuestionPurpose.LEARNING).stream()
				.map(Question::getId)
				.toList();
		return (int) questionTermRepository.findAllByQuestionIdIn(questionIds).stream()
				.map(QuestionTerm::getTermId)
				.distinct()
				.count();
	}

	private static long toYyyymmdd(LocalDate date) {
		return Long.parseLong(date.format(DateTimeFormatter.BASIC_ISO_DATE));
	}
}
