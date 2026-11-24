package com.dotorich.learning.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import com.dotorich.streak.service.StreakBreakService;

import lombok.RequiredArgsConstructor;

/**
 * 단계 완수 (P2).
 * 0. 끊김 판정을 먼저 한다 (홈을 거치지 않고 바로 학습해도 보호권 사용·벌칙이 빠지지 않게)
 * 1. 단계를 COMPLETED 로, 같은 섹터 다음 단계가 잠겨 있으면 UNLOCKED 로
 * 2. 하루 학습이고 오늘 첫 학습이면 학습일 기록, 스트릭, 나무 1칸 + 도토리 +1, 7칸이면 수확 보너스 +5 와 새 나무
 *    - 하루 학습: 평일은 오늘의 섹터, 일요일(오늘의 섹터 없음)은 아무 섹터
 *    - 이미 끝낸 단계(완료·레벨 테스트 통과)를 다시 풀면, 그 섹터의 모든 단계를 끝낸 경우에만 인정
 * 3. 그 외(추가 풀이, 오늘 이미 학습)는 1번만
 *
 * 같은 사용자의 단계 완료·홈 끊김 판정·보호권 구매는 user_streaks 행 잠금으로 한 번에 하나씩 처리된다.
 * MySQL 은 트랜잭션의 첫 일반 조회 시점의 데이터를 보므로, 잠금을 다른 조회보다 먼저 건다
 * (그래야 두 번째 요청이 첫 요청이 연 다음 단계를 보고 중복 저장하지 않는다).
 */
@Service
@RequiredArgsConstructor
public class StageCompleteService {

	private final StreakBreakService streakBreakService;
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
		LocalDateTime now = LocalDateTime.now(KstTime.ZONE);
		LocalDate today = now.toLocalDate();

		// 0. 끊김 판정 → 스트릭 잠금 (다른 조회보다 먼저)
		streakBreakService.check(userId, today);
		UserStreak streak = userStreakRepository.findByIdForUpdate(userId)
				.orElseGet(() -> userStreakRepository.save(new UserStreak(userId)));

		Stage stage = stageRepository.findById(stageId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
		UserStageProgress progress = progressRepository.findByUserIdAndStageId(userId, stageId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STAGE_LOCKED));

		// 상태를 바꾸기 전에 하루 학습인지 판단한다 (다시 푼 단계인지 알아야 해서)
		boolean replay = isDone(progress.getStatus());
		boolean dailyStudy = isDailyStudy(userId, stage, replay, today);

		// 1. 단계 완수, 다음 단계 열기
		progress.complete(now);
		Long nextStageId = unlockNextStage(userId, stage);

		// 2. 하루 학습 보상
		boolean dailyRewarded = false;
		int acornEarned = 0;
		boolean treeCompleted = false;
		int treeBonus = 0;
		Tree tree = treeRepository.findFirstByUserIdAndCompletedAtIsNull(userId).orElse(null);

		if (dailyStudy && !streak.studiedOn(today)) {
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
					treeBonus = AcornService.TREE_COMPLETE_BONUS;
					acornEarned += treeBonus;
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
				(int) treeRepository.countByUserIdAndCompletedAtIsNotNull(userId),
				treeBonus,
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

	/**
	 * 하루 학습인지. 평일은 오늘의 섹터, 일요일(오늘의 섹터 없음)은 아무 섹터.
	 * 이미 끝낸 단계를 다시 푼 경우는 그 섹터의 모든 단계를 끝냈을 때만 인정한다.
	 */
	private boolean isDailyStudy(Long userId, Stage stage, boolean replay, LocalDate today) {
		Optional<Sector> todaySector = todaySectorFinder.find(today);
		if (todaySector.isPresent() && !todaySector.get().getId().equals(stage.getSectorId())) {
			return false;
		}
		return !replay || isSectorDone(userId, stage.getSectorId());
	}

	/** 그 섹터의 모든 단계를 끝냈는지 (완료 또는 레벨 테스트 통과) */
	private boolean isSectorDone(Long userId, Long sectorId) {
		List<Long> stageIds = stageRepository.findAllBySectorIdOrderByStepNoAsc(sectorId).stream()
				.map(Stage::getId)
				.toList();
		Map<Long, StageStatus> statusByStageId = progressRepository.findAllByUserIdAndStageIdIn(userId, stageIds).stream()
				.collect(Collectors.toMap(UserStageProgress::getStageId, UserStageProgress::getStatus));
		return stageIds.stream().allMatch(id -> isDone(statusByStageId.get(id)));
	}

	/** 완료했거나 레벨 테스트로 통과한 단계 */
	private static boolean isDone(StageStatus status) {
		return status == StageStatus.COMPLETED || status == StageStatus.PASSED_BY_TEST;
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
