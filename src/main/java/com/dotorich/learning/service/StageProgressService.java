package com.dotorich.learning.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.learning.domain.Stage;
import com.dotorich.learning.domain.StageStatus;
import com.dotorich.learning.domain.UserStageProgress;
import com.dotorich.learning.repository.StageRepository;
import com.dotorich.learning.repository.UserStageProgressRepository;

import lombok.RequiredArgsConstructor;

/** 단계 상태 저장 (P2). 단계 상태는 Part 2 에서만 저장한다. */
@Service
@RequiredArgsConstructor
public class StageProgressService {

	private final StageRepository stageRepository;
	private final UserStageProgressRepository progressRepository;

	/**
	 * 레벨 테스트 결과로 섹터별 시작 단계를 저장한다 (Part 3 레벨 테스트 결과 제출·건너뛰기에서 호출).
	 * key = sectorId, value = 시작 단계 번호(1~3). 건너뛰기는 모든 섹터를 1로 넘긴다.
	 * 시작 단계 앞은 PASSED_BY_TEST, 시작 단계는 UNLOCKED, 그 뒤는 저장하지 않는다(잠김).
	 * 이미 단계 기록이 있는 사용자면 아무것도 하지 않는다 (Part 3 의 409 와 함께 이중으로 막는 안전장치).
	 * 시작 단계 번호가 그 섹터에 없으면 INVALID_INPUT.
	 */
	@Transactional
	public void initStartStages(Long userId, Map<Long, Integer> startStepBySector) {
		if (progressRepository.existsByUserId(userId)) {
			return;
		}

		List<UserStageProgress> progresses = new ArrayList<>();
		startStepBySector.forEach((sectorId, startStep) -> {
			List<Stage> stages = stageRepository.findAllBySectorIdOrderByStepNoAsc(sectorId);
			boolean startExists = stages.stream().anyMatch(stage -> stage.getStepNo() == startStep);
			if (!startExists) {
				throw new BusinessException(ErrorCode.INVALID_INPUT);
			}

			for (Stage stage : stages) {
				if (stage.getStepNo() < startStep) {
					progresses.add(new UserStageProgress(userId, stage.getId(), StageStatus.PASSED_BY_TEST));
				} else if (stage.getStepNo() == startStep) {
					progresses.add(new UserStageProgress(userId, stage.getId(), StageStatus.UNLOCKED));
				}
			}
		});

		progressRepository.saveAll(progresses);
	}
}
