package com.dotorich.learning.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.util.KstTime;
import com.dotorich.home.service.LearningStatusReader;
import com.dotorich.learning.domain.StageStatus;
import com.dotorich.learning.domain.UserStageProgress;
import com.dotorich.learning.repository.SectorRepository;
import com.dotorich.learning.repository.StageRepository;
import com.dotorich.learning.repository.UserStageProgressRepository;
import com.dotorich.review.domain.WrongAnswerStatus;
import com.dotorich.review.repository.WrongAnswerRepository;
import com.dotorich.streak.domain.StudyDay;
import com.dotorich.streak.domain.Tree;
import com.dotorich.streak.domain.UserStreak;
import com.dotorich.streak.repository.StudyDayRepository;
import com.dotorich.streak.repository.TreeRepository;
import com.dotorich.streak.repository.UserStreakRepository;
import com.dotorich.streak.service.StreakBreakService;

import lombok.RequiredArgsConstructor;

/**
 * 홈·마이페이지에 들어갈 Part 2 학습 데이터 (Part 1 HomeService 가 부른다).
 * read() 는 홈을 열 때 끊김 판정까지 한다.
 *
 * 이 클래스 안에서 짧은 이름 Sector, Stage 는 LearningStatusReader 의 응답용 record 다.
 * 엔티티는 이름이 겹쳐서 com.dotorich.learning.domain.Sector / Stage 로 전체 이름을 쓴다.
 */
@Service
@RequiredArgsConstructor
public class LearningStatusService implements LearningStatusReader {

	private final StreakBreakService streakBreakService;
	private final TodaySectorFinder todaySectorFinder;
	private final SectorRepository sectorRepository;
	private final StageRepository stageRepository;
	private final UserStageProgressRepository progressRepository;
	private final UserStreakRepository userStreakRepository;
	private final StudyDayRepository studyDayRepository;
	private final TreeRepository treeRepository;
	private final WrongAnswerRepository wrongAnswerRepository;

	@Override
	@Transactional
	public Home read(Long userId) {
		LocalDate today = LocalDate.now(KstTime.ZONE);

		// 끊김 판정을 먼저 해야 아래 스트릭·나무 값이 처리 후 값으로 나간다
		boolean acornFell = streakBreakService.check(userId, today);

		Optional<UserStreak> streak = userStreakRepository.findById(userId);
		Optional<com.dotorich.learning.domain.Sector> todaySector = todaySectorFinder.find(today);
		Optional<Tree> tree = treeRepository.findFirstByUserIdAndCompletedAtIsNull(userId);
		int cardReviewCount = (int) wrongAnswerRepository.countByUserIdAndStatus(userId, WrongAnswerStatus.CARD_REVIEW);

		return new Home(
				todaySector.map(s -> new Sector(s.getId(), s.getName())).orElse(null),
				streak.map(s -> s.studiedOn(today)).orElse(false),
				todaySector.map(s -> findNextStage(userId, s.getId())).orElse(null),
				cardReviewCount > 0,
				cardReviewCount,
				streak.map(UserStreak::getCurrentCount).orElse(0),
				weekStudied(userId, today),
				streak.map(UserStreak::getShieldCount).orElse(0),
				tree.map(Tree::getFilledCount).orElse(0),
				(int) treeRepository.countByUserIdAndCompletedAtIsNotNull(userId),
				tree.map(Tree::getFallenCount).orElse(0),
				acornFell);
	}

	@Override
	@Transactional(readOnly = true)
	public Stats stats(Long userId) {
		Map<Long, StageStatus> statusByStageId = progressRepository.findAllByUserId(userId).stream()
				.collect(Collectors.toMap(UserStageProgress::getStageId, UserStageProgress::getStatus));

		int completedStageCount = (int) statusByStageId.values().stream()
				.filter(status -> status == StageStatus.COMPLETED)
				.count();

		List<SectorProgress> sectors = sectorRepository.findAll(Sort.by("id")).stream()
				.map(sector -> {
					List<com.dotorich.learning.domain.Stage> stages =
							stageRepository.findAllBySectorIdOrderByStepNoAsc(sector.getId());
					int passed = (int) stages.stream()
							.filter(stage -> isDone(statusByStageId.get(stage.getId())))
							.count();
					return new SectorProgress(sector.getId(), sector.getName(), passed, stages.size());
				})
				.toList();

		int streakCount = userStreakRepository.findById(userId).map(UserStreak::getCurrentCount).orElse(0);
		return new Stats(streakCount, completedStageCount, sectors);
	}

	/** 오늘의 섹터에서 완료·통과 안 한 가장 앞 단계. 다 끝냈으면 null */
	private Stage findNextStage(Long userId, Long sectorId) {
		List<com.dotorich.learning.domain.Stage> stages = stageRepository.findAllBySectorIdOrderByStepNoAsc(sectorId);
		List<Long> stageIds = stages.stream().map(com.dotorich.learning.domain.Stage::getId).toList();
		Map<Long, StageStatus> statusByStageId = progressRepository.findAllByUserIdAndStageIdIn(userId, stageIds).stream()
				.collect(Collectors.toMap(UserStageProgress::getStageId, UserStageProgress::getStatus));

		return stages.stream()
				.filter(stage -> !isDone(statusByStageId.get(stage.getId())))
				.findFirst()
				.map(stage -> new Stage(stage.getId(), stage.getStepNo(), stage.getTitle()))
				.orElse(null);
	}

	/** 이번 주 월~일 학습 여부 7개 (오늘 이후는 기록이 없으니 false) */
	private List<Boolean> weekStudied(Long userId, LocalDate today) {
		LocalDate monday = today.with(DayOfWeek.MONDAY);
		Set<LocalDate> studied = studyDayRepository
				.findAllByUserIdAndStudyDateBetween(userId, monday, monday.plusDays(6)).stream()
				.map(StudyDay::getStudyDate)
				.collect(Collectors.toSet());
		return monday.datesUntil(monday.plusDays(7))
				.map(studied::contains)
				.toList();
	}

	/** 완료했거나 레벨 테스트로 통과한 단계 */
	private static boolean isDone(StageStatus status) {
		return status == StageStatus.COMPLETED || status == StageStatus.PASSED_BY_TEST;
	}
}
