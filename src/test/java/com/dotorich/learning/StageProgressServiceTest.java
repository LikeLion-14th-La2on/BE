package com.dotorich.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.learning.domain.Stage;
import com.dotorich.learning.domain.UserStageProgress;
import com.dotorich.learning.repository.StageRepository;
import com.dotorich.learning.repository.UserStageProgressRepository;
import com.dotorich.learning.service.StageProgressService;

class StageProgressServiceTest {

	private UserStageProgressRepository progressRepository;
	private StageProgressService service;

	@BeforeEach
	void setUp() {
		StageRepository stageRepository = mock(StageRepository.class);
		progressRepository = mock(UserStageProgressRepository.class);

		// 시드와 같은 번호: 주식 1~3, 생활 금융 4~6, 금융 기초 7~9
		when(stageRepository.findAllBySectorIdOrderByStepNoAsc(1L)).thenReturn(stagesOf(1L, 1L));
		when(stageRepository.findAllBySectorIdOrderByStepNoAsc(2L)).thenReturn(stagesOf(2L, 4L));
		when(stageRepository.findAllBySectorIdOrderByStepNoAsc(3L)).thenReturn(stagesOf(3L, 7L));

		service = new StageProgressService(stageRepository, progressRepository);
	}

	@Test
	void 시작_단계_앞은_통과_시작_단계는_열림_뒤는_저장하지_않는다() {
		Map<Long, Integer> start = new LinkedHashMap<>();
		start.put(1L, 2); // 주식 2단계부터
		start.put(2L, 1); // 생활 금융 1단계부터
		start.put(3L, 3); // 금융 기초 3단계부터

		service.initStartStages(1L, start);

		assertEquals(List.of(
				"1:PASSED_BY_TEST", "2:UNLOCKED",
				"4:UNLOCKED",
				"7:PASSED_BY_TEST", "8:PASSED_BY_TEST", "9:UNLOCKED"), savedRows());
	}

	@Test
	void 건너뛰기는_모든_섹터_1단계만_열린다() {
		service.initStartStages(1L, Map.of(1L, 1, 2L, 1, 3L, 1));

		assertEquals(List.of("1:UNLOCKED", "4:UNLOCKED", "7:UNLOCKED"),
				savedRows().stream().sorted().toList());
	}

	@Test
	void 이미_단계_기록이_있으면_아무것도_저장하지_않는다() {
		when(progressRepository.existsByUserId(1L)).thenReturn(true);

		service.initStartStages(1L, Map.of(1L, 2));

		verify(progressRepository, never()).saveAll(any());
	}

	@Test
	void 없는_단계_번호면_400() {
		assertThrows(BusinessException.class, () -> service.initStartStages(1L, Map.of(1L, 5)));
		verify(progressRepository, never()).saveAll(any());
	}

	/** 저장된 행을 "단계id:상태" 글자로 */
	@SuppressWarnings("unchecked")
	private List<String> savedRows() {
		ArgumentCaptor<List<UserStageProgress>> captor = ArgumentCaptor.forClass(List.class);
		verify(progressRepository).saveAll(captor.capture());
		return captor.getValue().stream()
				.map(p -> p.getStageId() + ":" + p.getStatus())
				.toList();
	}

	private static List<Stage> stagesOf(Long sectorId, Long firstStageId) {
		return List.of(
				new Stage(firstStageId, sectorId, 1, "1단계"),
				new Stage(firstStageId + 1, sectorId, 2, "2단계"),
				new Stage(firstStageId + 2, sectorId, 3, "3단계"));
	}
}
