package com.dotorich.learning.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.util.KstTime;
import com.dotorich.learning.domain.Sector;
import com.dotorich.learning.dto.SectorListResponse;
import com.dotorich.learning.dto.SectorListResponse.SectorItem;
import com.dotorich.learning.repository.SectorRepository;
import com.dotorich.streak.repository.UserStreakRepository;

import lombok.RequiredArgsConstructor;

/** 섹터 목록 (P2) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SectorService {

	private final SectorRepository sectorRepository;
	private final TodaySectorFinder todaySectorFinder;
	private final UserStreakRepository userStreakRepository;

	public SectorListResponse getSectors(Long userId) {
		LocalDate today = LocalDate.now(KstTime.ZONE);

		// 오늘의 섹터 id (일요일이면 null)
		Long todaySectorId = todaySectorFinder.find(today).map(Sector::getId).orElse(null);

		// 오늘 학습했는지 (기록이 없으면 false)
		boolean todayCompleted = userStreakRepository.findById(userId)
				.map(streak -> streak.studiedOn(today))
				.orElse(false);

		List<SectorItem> sectors = sectorRepository.findAll(Sort.by("id")).stream()
				.map(sector -> new SectorItem(sector.getId(), sector.getName(), sector.getId().equals(todaySectorId)))
				.toList();

		return new SectorListResponse(todayCompleted, sectors);
	}
}
