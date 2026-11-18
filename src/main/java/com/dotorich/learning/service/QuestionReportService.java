package com.dotorich.learning.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.learning.domain.QuestionReport;
import com.dotorich.learning.repository.QuestionReportRepository;
import com.dotorich.question.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

/**
 * 문제 신고 (P2, 2순위).
 * 모든 문제(학습·레벨 테스트·시사 퀴즈)를 신고할 수 있고, 같은 문제를 여러 번 신고해도 1건씩 저장한다.
 */
@Service
@RequiredArgsConstructor
public class QuestionReportService {

	private final QuestionRepository questionRepository;
	private final QuestionReportRepository reportRepository;

	@Transactional
	public void report(Long userId, Long questionId, String content) {
		if (!questionRepository.existsById(questionId)) {
			throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
		}
		reportRepository.save(new QuestionReport(userId, questionId, content.trim(), LocalDateTime.now(KstTime.ZONE)));
	}
}
