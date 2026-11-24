package com.dotorich.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.tool.domain.ToolType;
import com.dotorich.tool.dto.ToolConditionRequest;
import com.dotorich.tool.repository.ToolConditionRepository;
import com.dotorich.tool.service.ToolConditionService;

import tools.jackson.databind.json.JsonMapper;

class ToolConditionServiceTest {

	private final ToolConditionRepository repository = mock(ToolConditionRepository.class);
	private final ToolConditionService service = new ToolConditionService(repository, JsonMapper.builder().build());

	@Test
	void 저장한_적_없으면_null() {
		when(repository.findByUserIdAndToolType(anyLong(), any())).thenReturn(Optional.empty());
		assertNull(service.get(1L, ToolType.COMPOUND));
	}

	@Test
	void params_를_JSON_그대로_덮어쓰기_저장() {
		service.save(1L, new ToolConditionRequest(ToolType.COMPOUND, Map.of("principal", 1000000)));
		verify(repository).upsert(1L, "COMPOUND", "{\"principal\":1000000}");
	}

	@Test
	void 너무_큰_params_는_400() {
		ToolConditionRequest big = new ToolConditionRequest(ToolType.LOAN, Map.of("memo", "x".repeat(2000)));
		BusinessException e = assertThrows(BusinessException.class, () -> service.save(1L, big));
		assertEquals("E400", e.getErrorCode().getCode());
		verify(repository, never()).upsert(anyLong(), anyString(), anyString());
	}
}
