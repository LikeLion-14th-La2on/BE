package com.dotorich.home.service;

import java.util.List;

/**
 * 홈·마이페이지 학습 현황에서 Part 2 데이터를 읽는 창구.
 * Part 2(정예원)가 learning/streak/review 데이터로 구현하고, 임시 구현(TempLearningStatusReader)은 지운다.
 * 접속 시 끊김 판정(나무 차감 + AcornService.penalizeStreakBreak 호출)도 read() 안에서 처리하면 된다.
 */
public interface LearningStatusReader {

	Home read(Long userId);

	Stats stats(Long userId);

	record Home(
		Sector todaySector,
		boolean todayCompleted,
		Stage nextStage,           // 오늘의 섹터에서 아직 안 끝낸 가장 앞 단계, 다 끝냈으면 null
		boolean hasCardReview,     // 전날 오답 카드뉴스 복습이 있는지
		int yesterdayWrongCount,   // 홈 틀린 문제 카드에 쓰는 개수
		int streakCount,
		int shieldCount,
		int acornCount,            // 지금 나무에 열린 도토리 0~7
		int treeCount,             // 내 숲에 모은 나무 수
		boolean acornFell          // 마지막 확인 뒤 끊겨서 도토리가 떨어졌는지
	) {
	}

	record Sector(Long sectorId, String name) {
	}

	record Stage(Long stageId, int stepNo, String title) {
	}

	record Stats(int streak, int completedStageCount, List<SectorProgress> sectors) {
	}

	record SectorProgress(Long sectorId, String name, int passedStepNo, int totalSteps) {
	}
}
