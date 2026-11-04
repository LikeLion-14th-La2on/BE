package com.dotorich.home.dto;

import com.dotorich.home.service.LearningStatusReader;

public record HomeResponse(
	String nickname,
	LearningStatusReader.Sector todaySector,
	boolean todayCompleted,
	LearningStatusReader.Stage nextStage,
	boolean hasCardReview,
	int yesterdayWrongCount,
	Streak streak,
	Tree tree,
	int acornBalance,
	String characterImageUrl
) {

	public record Streak(int currentCount, int shieldCount) {
	}

	public record Tree(int acornCount, int treeCount, boolean acornFell) {
	}
}
