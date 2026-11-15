package com.dotorich.learning.domain;

/**
 * 단계 상태.
 * LOCKED 는 DB에 저장하지 않는다. user_stage_progress 에 행이 없으면 LOCKED 다.
 */
public enum StageStatus {
	LOCKED,
	UNLOCKED,
	PASSED_BY_TEST,
	COMPLETED
}
