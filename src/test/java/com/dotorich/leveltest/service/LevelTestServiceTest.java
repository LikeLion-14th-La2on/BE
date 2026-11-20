package com.dotorich.leveltest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.learning.service.StageReader.StageInfo;
import com.dotorich.leveltest.dto.LevelTestResultResponse;
import com.dotorich.leveltest.dto.LevelTestSubmitRequest;
import com.dotorich.leveltest.dto.LevelTestSubmitRequest.Answer;
import com.dotorich.leveltest.service.LevelTestStageGateway.SectorInfo;
import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.domain.QuestionType;
import com.dotorich.question.repository.QuestionRepository;
import com.dotorich.user.domain.LevelTestStatus;
import com.dotorich.user.service.UserService;

class LevelTestServiceTest {

	private QuestionRepository questionRepository;
	private LevelTestStageGateway gateway;
	private UserService userService;
	private LevelTestService service;

	@BeforeEach
	void setUp() {
		questionRepository = mock(QuestionRepository.class);
		gateway = mock(LevelTestStageGateway.class);
		userService = mock(UserService.class);
		service = new LevelTestService(questionRepository, gateway, userService);

		// 섹터 1(주식): 단계 id 1·2·3 = 1·2·3단계, 단계마다 OX 2문제 (정답 O)
		when(gateway.getAllSectors()).thenReturn(List.of(new SectorInfo(1L, "주식")));
		for (long stageId = 1; stageId <= 3; stageId++) {
			when(gateway.getStageInfo(stageId)).thenReturn(new StageInfo(stageId, 1L, (int)stageId));
		}
		List<Question> questions = new ArrayList<>();
		for (long id = 1; id <= 6; id++) {
			long stageId = (id + 1) / 2;
			questions.add(new Question(id, stageId, QuestionPurpose.LEVEL_TEST, QuestionType.OX, "문제" + id,
				null, "true", "해설" + id, null));
		}
		when(questionRepository.findAllByPurposeOrderByStageIdAscIdAsc(QuestionPurpose.LEVEL_TEST))
			.thenReturn(questions);
	}

	private static LevelTestSubmitRequest answers(String... byQuestion) {
		List<Answer> list = new ArrayList<>();
		for (int i = 0; i < byQuestion.length; i++) {
			list.add(new Answer((long)(i + 1), byQuestion[i]));
		}
		return new LevelTestSubmitRequest(list);
	}

	@Test
	void 시작_단계_규칙() {
		assertEquals(2, LevelTestService.startStep(List.of(true, false, true)));
		assertEquals(1, LevelTestService.startStep(List.of(false, true, true)));
		assertEquals(3, LevelTestService.startStep(List.of(true, true, false)));
		assertEquals(3, LevelTestService.startStep(List.of(true, true, true)));
	}

	@Test
	void 한_문제만_맞혀도_그_단계는_통과_못_한다() {
		// 1단계 2문제 정답, 2단계 1문제만 정답 → 2단계부터
		LevelTestResultResponse result = service.submit(1L, answers("O", "O", "O", "X", "O", "O"));

		assertEquals(2, result.results().get(0).startStepNo());
		assertEquals(6, result.explanations().size());
		verify(userService).completeLevelTest(1L, LevelTestStatus.DONE);
		verify(gateway).initStartStages(1L, Map.of(1L, 2));
	}

	@Test
	void 잘_모르겠어요는_오답이라_1단계부터() {
		LevelTestResultResponse result = service.submit(1L, answers("UNKNOWN", "O", "O", "O", "O", "O"));
		assertEquals(1, result.results().get(0).startStepNo());
	}

	@Test
	void 레벨_테스트에_없는_문제_id면_400() {
		LevelTestSubmitRequest request = new LevelTestSubmitRequest(List.of(new Answer(999L, "O")));
		BusinessException e = assertThrows(BusinessException.class, () -> service.submit(1L, request));
		assertEquals(ErrorCode.INVALID_INPUT, e.getErrorCode());
		verify(gateway, never()).initStartStages(anyLong(), any());
	}

	@Test
	void 건너뛰기는_모든_섹터_1단계() {
		service.skip(1L);
		verify(userService).completeLevelTest(1L, LevelTestStatus.SKIPPED);
		verify(gateway).initStartStages(1L, Map.of(1L, 1));
	}
}
