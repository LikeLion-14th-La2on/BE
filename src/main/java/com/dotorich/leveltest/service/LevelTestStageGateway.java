package com.dotorich.leveltest.service;

import java.util.List;
import java.util.Map;

import com.dotorich.learning.service.StageReader.StageInfo;

/**
 * 레벨 테스트가 Part 2(섹터·단계)에 묻고 저장하는 창구.
 * 지금은 TempLevelTestStageGateway 가 구현한다. Part 2 함수가 다 나오면 그 파일만 바꾼다.
 */
public interface LevelTestStageGateway {

	/** 전체 섹터 (번호 순) */
	List<SectorInfo> getAllSectors();

	/** 단계 → 섹터 id, 단계 번호 */
	StageInfo getStageInfo(Long stageId);

	/** 섹터별 시작 단계 저장. key = sectorId, value = 시작 단계 1~3 */
	void initStartStages(Long userId, Map<Long, Integer> startStepBySector);

	record SectorInfo(Long sectorId, String name) {
	}
}
