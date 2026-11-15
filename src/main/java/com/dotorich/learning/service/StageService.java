package com.dotorich.learning.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.learning.domain.Stage;
import com.dotorich.learning.domain.StageStatus;
import com.dotorich.learning.domain.UserStageProgress;
import com.dotorich.learning.dto.StageResponse;
import com.dotorich.learning.repository.SectorRepository;
import com.dotorich.learning.repository.StageRepository;
import com.dotorich.learning.repository.UserStageProgressRepository;

import lombok.RequiredArgsConstructor;

/** 섹터별 단계 목록 (P2) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageService {

	private final SectorRepository sectorRepository;
	private final StageRepository stageRepository;
	private final UserStageProgressRepository progressRepository;

	public List<StageResponse> getStages(Long userId, Long sectorId) {
		if (!sectorRepository.existsById(sectorId)) {
			throw new BusinessException(ErrorCode.SECTOR_NOT_FOUND);
		}

		List<Stage> stages = stageRepository.findAllBySectorIdOrderByStepNoAsc(sectorId);
		List<Long> stageIds = stages.stream().map(Stage::getId).toList();

		// 단계 id → 내 상태 (기록이 있는 단계만 들어 있다)
		Map<Long, StageStatus> statusByStageId = progressRepository
				.findAllByUserIdAndStageIdIn(userId, stageIds).stream()
				.collect(Collectors.toMap(UserStageProgress::getStageId, UserStageProgress::getStatus));

		// 기록이 없는 단계는 LOCKED
		return stages.stream()
				.map(stage -> new StageResponse(stage.getId(), stage.getStepNo(), stage.getTitle(),
						statusByStageId.getOrDefault(stage.getId(), StageStatus.LOCKED)))
				.toList();
	}
}
