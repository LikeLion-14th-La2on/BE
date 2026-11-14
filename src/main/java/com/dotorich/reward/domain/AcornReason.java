package com.dotorich.reward.domain;

/**
 * 도토리 내역 사유. target_id 규칙
 * DAILY_STUDY=학습한 날짜(yyyymmdd, 하루 1번), CHARACTER=character_id, TREE_COMPLETE=나무 번호,
 * STREAK_BREAK=끊긴 날짜(yyyymmdd), AI_TUTOR·STREAK_SHIELD=null(여러 번 허용)
 */
public enum AcornReason {
	DAILY_STUDY,
	TREE_COMPLETE,
	STREAK_BREAK,
	AI_TUTOR,
	CHARACTER,
	STREAK_SHIELD
}
