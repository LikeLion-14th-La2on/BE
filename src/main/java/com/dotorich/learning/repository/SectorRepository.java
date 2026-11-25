package com.dotorich.learning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.learning.domain.Sector;

public interface SectorRepository extends JpaRepository<Sector, Long> {

	/** 요일 순환 순서대로 전체 섹터 */
	List<Sector> findAllByOrderByRotationOrderAsc();
}
