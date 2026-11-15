package com.dotorich.learning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.learning.domain.Stage;

public interface StageRepository extends JpaRepository<Stage, Long> {

	/** 한 섹터의 단계 (단계 번호 순) */
	List<Stage> findAllBySectorIdOrderByStepNoAsc(Long sectorId);
}
