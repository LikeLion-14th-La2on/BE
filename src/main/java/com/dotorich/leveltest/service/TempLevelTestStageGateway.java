package com.dotorich.leveltest.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dotorich.learning.service.StageReader;
import com.dotorich.learning.service.StageReader.StageInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ponytail: Part 2 함수가 나오기 전 임시 구현.
 * - 섹터 이름: StageReader 에 이름 조회 함수가 없어 시드(data.sql) 이름을 임시로 쓴다.
 * - initStartStages: learning.service.StageProgressService.initStartStages 가 나오면 그걸 부르도록 바꾼다.
 *   지금은 저장하지 않고 로그만 남긴다. (레벨 테스트 상태 DONE/SKIPPED 는 정상 저장된다)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempLevelTestStageGateway implements LevelTestStageGateway {

	private static final Map<Long, String> SECTOR_NAMES = Map.of(1L, "주식", 2L, "생활 금융", 3L, "금융 기초");

	private final StageReader stageReader;

	@Override
	public List<SectorInfo> getAllSectors() {
		return stageReader.getAllSectorIds().stream()
			.map(id -> new SectorInfo(id, SECTOR_NAMES.getOrDefault(id, "섹터 " + id)))
			.toList();
	}

	@Override
	public StageInfo getStageInfo(Long stageId) {
		return stageReader.getStageInfo(stageId);
	}

	@Override
	public void initStartStages(Long userId, Map<Long, Integer> startStepBySector) {
		log.warn("[임시] Part 2 initStartStages 미구현: userId={}, 시작 단계={}", userId, startStepBySector);
	}
}
