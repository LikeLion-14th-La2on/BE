package com.dotorich.leveltest.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dotorich.learning.service.StageProgressService;
import com.dotorich.learning.service.StageReader;
import com.dotorich.learning.service.StageReader.StageInfo;

import lombok.RequiredArgsConstructor;

/** 레벨 테스트 → Part 2 연결. 섹터·단계 조회는 StageReader, 시작 단계 저장은 StageProgressService. */
@Component
@RequiredArgsConstructor
public class LearningStageGateway implements LevelTestStageGateway {

	private final StageReader stageReader;
	private final StageProgressService stageProgressService;

	@Override
	public List<SectorInfo> getAllSectors() {
		return stageReader.getAllSectors().stream()
			.map(sector -> new SectorInfo(sector.sectorId(), sector.name()))
			.toList();
	}

	@Override
	public StageInfo getStageInfo(Long stageId) {
		return stageReader.getStageInfo(stageId);
	}

	@Override
	public void initStartStages(Long userId, Map<Long, Integer> startStepBySector) {
		stageProgressService.initStartStages(userId, startStepBySector);
	}
}
