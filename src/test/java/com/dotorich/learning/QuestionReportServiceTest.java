package com.dotorich.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.learning.domain.QuestionReport;
import com.dotorich.learning.repository.QuestionReportRepository;
import com.dotorich.learning.service.QuestionReportService;
import com.dotorich.question.repository.QuestionRepository;

class QuestionReportServiceTest {

	private final QuestionRepository questionRepository = mock(QuestionRepository.class);
	private final QuestionReportRepository reportRepository = mock(QuestionReportRepository.class);
	private final QuestionReportService service = new QuestionReportService(questionRepository, reportRepository);

	@Test
	void 없는_문제를_신고하면_QUESTION_NOT_FOUND() {
		when(questionRepository.existsById(99L)).thenReturn(false);

		BusinessException e = assertThrows(BusinessException.class, () -> service.report(1L, 99L, "오타"));
		assertEquals(ErrorCode.QUESTION_NOT_FOUND, e.getErrorCode());
		verify(reportRepository, never()).save(any());
	}

	@Test
	void 신고_내용은_앞뒤_공백을_지우고_저장() {
		when(questionRepository.existsById(10L)).thenReturn(true);

		service.report(1L, 10L, "  보기 2번 오타  ");

		ArgumentCaptor<QuestionReport> saved = ArgumentCaptor.forClass(QuestionReport.class);
		verify(reportRepository).save(saved.capture());
		assertEquals(1L, saved.getValue().getUserId());
		assertEquals(10L, saved.getValue().getQuestionId());
		assertEquals("보기 2번 오타", saved.getValue().getContent());
	}
}
