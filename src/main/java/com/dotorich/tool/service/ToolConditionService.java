package com.dotorich.tool.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.tool.domain.ToolType;
import com.dotorich.tool.dto.ToolConditionRequest;
import com.dotorich.tool.dto.ToolConditionResponse;
import com.dotorich.tool.repository.ToolConditionRepository;
import com.dotorich.user.domain.UserDeletedEvent;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/** 금융 도구 내 조건 저장·조회 (P1, 2순위) */
@Service
@RequiredArgsConstructor
public class ToolConditionService {

	/** 입력값 몇 개라 이 정도면 충분하다. 큰 JSON 을 쌓아 두는 용도로 쓰지 못하게 막는다 */
	static final int MAX_PARAMS_LENGTH = 1000;

	private final ToolConditionRepository repository;
	private final ObjectMapper objectMapper;

	/** 저장한 적이 없으면 null */
	@Transactional(readOnly = true)
	public ToolConditionResponse get(Long userId, ToolType toolType) {
		return repository.findByUserIdAndToolType(userId, toolType)
			.map(c -> new ToolConditionResponse(c.getToolType(), c.getParams()))
			.orElse(null);
	}

	@Transactional
	public void save(Long userId, ToolConditionRequest request) {
		String params = objectMapper.writeValueAsString(request.params());
		if (params.length() > MAX_PARAMS_LENGTH) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		repository.upsert(userId, request.toolType().name(), params);
	}

	@EventListener
	public void onUserDeleted(UserDeletedEvent event) {
		repository.deleteAllByUserId(event.userId());
	}
}
