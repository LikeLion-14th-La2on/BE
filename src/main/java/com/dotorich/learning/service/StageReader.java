package com.dotorich.learning.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.learning.domain.Stage;
import com.dotorich.learning.repository.SectorRepository;
import com.dotorich.learning.repository.StageRepository;

import lombok.RequiredArgsConstructor;

/**
 * 다른 파트가 섹터·단계 정보를 읽는 창구 (읽기 전용).
 * Part 3 레벨 테스트 채점에서 쓴다. Repository 를 직접 쓰지 말고 이 함수를 쓴다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageReader {

    private final SectorRepository sectorRepository;
    private final StageRepository stageRepository;

    /** 전체 섹터 id (번호 순) */
    public List<Long> getAllSectorIds() {
        return sectorRepository.findAllIds();
    }

    /** 단계 → 소속 섹터 id, 단계 번호. 없는 단계면 STAGE_NOT_FOUND */
    public StageInfo getStageInfo(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
        return new StageInfo(stage.getId(), stage.getSectorId(), stage.getStepNo());
    }

    public record StageInfo(Long stageId, Long sectorId, int stepNo) {
    }
}