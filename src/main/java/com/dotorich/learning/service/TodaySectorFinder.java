package com.dotorich.learning.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.dotorich.common.util.KstTime;
import com.dotorich.learning.domain.Sector;
import com.dotorich.learning.repository.SectorRepository;

import lombok.RequiredArgsConstructor;

/**
 * 오늘의 섹터 계산 (KST 기준).
 * 월~토를 rotation_order 순서로 번갈아 배정한다: 월·목 1번, 화·금 2번, 수·토 3번.
 * 일요일은 복습하는 날이라 오늘의 섹터가 없다.
 */
@Component
@RequiredArgsConstructor
public class TodaySectorFinder {

	private final SectorRepository sectorRepository;

	/** 오늘의 섹터. 일요일이면 비어 있다 */
	public Optional<Sector> find() {
		return find(LocalDate.now(KstTime.ZONE));
	}

	/** 특정 날짜의 섹터 (테스트용으로 날짜를 받는다) */
	public Optional<Sector> find(LocalDate date) {
		if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return Optional.empty();
		}
		List<Sector> sectors = sectorRepository.findAllByOrderByRotationOrderAsc();
		if (sectors.isEmpty()) {
			return Optional.empty();
		}
		int index = (date.getDayOfWeek().getValue() - 1) % sectors.size();
		return Optional.of(sectors.get(index));
	}
}
