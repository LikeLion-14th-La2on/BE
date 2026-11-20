package com.dotorich.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dotorich.learning.domain.Sector;
import com.dotorich.learning.repository.SectorRepository;
import com.dotorich.learning.service.TodaySectorFinder;

class TodaySectorFinderTest {

	private TodaySectorFinder finder;

	@BeforeEach
	void setUp() {
		SectorRepository sectorRepository = mock(SectorRepository.class);
		when(sectorRepository.findAllByOrderByRotationOrderAsc()).thenReturn(List.of(
				new Sector(1L, "주식", 1),
				new Sector(2L, "생활 금융", 2),
				new Sector(3L, "금융 기초", 3)));
		finder = new TodaySectorFinder(sectorRepository);
	}

	@Test
	void 월요일과_목요일은_주식() {
		assertEquals(1L, finder.find(LocalDate.of(2026, 11, 16)).orElseThrow().getId());
		assertEquals(1L, finder.find(LocalDate.of(2026, 11, 19)).orElseThrow().getId());
	}

	@Test
	void 화요일과_금요일은_생활_금융() {
		assertEquals(2L, finder.find(LocalDate.of(2026, 11, 17)).orElseThrow().getId());
		assertEquals(2L, finder.find(LocalDate.of(2026, 11, 20)).orElseThrow().getId());
	}

	@Test
	void 수요일과_토요일은_금융_기초() {
		assertEquals(3L, finder.find(LocalDate.of(2026, 11, 18)).orElseThrow().getId());
		assertEquals(3L, finder.find(LocalDate.of(2026, 11, 21)).orElseThrow().getId());
	}

	@Test
	void 일요일은_오늘의_섹터가_없다() {
		assertTrue(finder.find(LocalDate.of(2026, 11, 15)).isEmpty());
	}
}
