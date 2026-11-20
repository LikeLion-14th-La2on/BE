package com.dotorich.tutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.reward.service.AcornService;
import com.dotorich.term.domain.Term;
import com.dotorich.term.service.TermService;
import com.dotorich.tutor.dto.TutorResponse;
import com.dotorich.tutor.service.TutorClient;
import com.dotorich.tutor.service.TutorService;
import com.dotorich.tutor.service.TutorUsageService;

class TutorServiceTest {

	private static final int FREE = 3;
	private static final int COST = 1;

	private TermService termService;
	private TutorUsageService usageService;
	private AcornService acornService;
	private TutorClient tutorClient;
	private TutorService tutorService;
	private final Term term = new Term(10L, "원리금균등상환", "대출 원금과 이자를 합친 금액을 매달 같은 금액으로 갚는 방식",
		"매달 똑같은 돈을 내는 방식", null, null);

	@BeforeEach
	void setUp() {
		termService = mock(TermService.class);
		usageService = mock(TutorUsageService.class);
		acornService = mock(AcornService.class);
		tutorClient = mock(TutorClient.class);
		tutorService = new TutorService(termService, usageService, acornService, tutorClient, FREE, COST);
		when(termService.getTerm(10L)).thenReturn(term);
		when(tutorClient.ask(eq(term), anyString())).thenReturn("답변");
	}

	@Test
	void 무료_횟수_안이면_도토리_없이_답한다() {
		when(usageService.getCount(anyLong(), any())).thenReturn(0);
		when(usageService.record(eq(1L), any(), eq(FREE), eq(COST)))
			.thenReturn(new TutorUsageService.Recorded(1, 0, 30));

		TutorResponse response = tutorService.ask(1L, 10L, " 이자가 적은 건? ");

		assertEquals("답변", response.answer());
		assertEquals(2, response.freeRemaining());
		assertEquals(0, response.acornSpent());
		assertEquals(30, response.acornBalance());
		verify(tutorClient).ask(term, "이자가 적은 건?");
	}

	@Test
	void 무료_횟수를_넘으면_차감된_도토리가_응답에_나온다() {
		when(usageService.getCount(anyLong(), any())).thenReturn(3);
		when(acornService.getBalance(1L)).thenReturn(5);
		when(usageService.record(eq(1L), any(), eq(FREE), eq(COST)))
			.thenReturn(new TutorUsageService.Recorded(4, 1, 4));

		TutorResponse response = tutorService.ask(1L, 10L, "질문");

		assertEquals(0, response.freeRemaining());
		assertEquals(1, response.acornSpent());
		assertEquals(4, response.acornBalance());
	}

	@Test
	void 무료_횟수를_넘었는데_도토리가_없으면_LLM을_부르지_않고_400() {
		when(usageService.getCount(anyLong(), any())).thenReturn(3);
		when(acornService.getBalance(1L)).thenReturn(0);

		BusinessException e = assertThrows(BusinessException.class, () -> tutorService.ask(1L, 10L, "질문"));

		assertEquals(ErrorCode.ACORN_NOT_ENOUGH, e.getErrorCode());
		verify(tutorClient, never()).ask(any(), anyString());
		verify(usageService, never()).record(anyLong(), any(), anyInt(), anyInt());
	}

	@Test
	void LLM이_실패하면_횟수도_도토리도_그대로() {
		when(usageService.getCount(anyLong(), any())).thenReturn(0);
		when(tutorClient.ask(eq(term), anyString())).thenThrow(new BusinessException(ErrorCode.TUTOR_UNAVAILABLE));

		BusinessException e = assertThrows(BusinessException.class, () -> tutorService.ask(1L, 10L, "질문"));

		assertEquals(ErrorCode.TUTOR_UNAVAILABLE, e.getErrorCode());
		verify(usageService, never()).record(anyLong(), any(), anyInt(), anyInt());
	}

	@Test
	void 없는_용어면_TERM_NOT_FOUND() {
		when(termService.getTerm(99L)).thenThrow(new BusinessException(ErrorCode.TERM_NOT_FOUND));

		assertThrows(BusinessException.class, () -> tutorService.ask(1L, 99L, "질문"));
		verify(tutorClient, never()).ask(any(), anyString());
	}
}
