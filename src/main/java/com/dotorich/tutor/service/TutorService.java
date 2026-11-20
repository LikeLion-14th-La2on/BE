package com.dotorich.tutor.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.reward.service.AcornService;
import com.dotorich.term.domain.Term;
import com.dotorich.term.service.TermService;
import com.dotorich.tutor.dto.TutorResponse;

/**
 * AI 튜터 (P3). 용어 기준으로 질문하고, 용어 DB 설명 안에서만 답한다.
 *
 * 1. 용어가 없으면 404
 * 2. 오늘 무료 횟수를 다 썼는데 도토리가 부족하면 LLM 을 부르기 전에 400
 * 3. LLM 호출 (실패하면 503, 횟수·도토리 그대로)
 * 4. 답을 받은 뒤에만 횟수 +1, 무료 초과분 도토리 차감
 *
 * LLM 호출(수 초)을 DB 트랜잭션·행 잠금 밖에서 해서, 그동안 다른 요청을 막지 않는다.
 */
@Service
public class TutorService {

	private final TermService termService;
	private final TutorUsageService usageService;
	private final AcornService acornService;
	private final TutorClient tutorClient;
	private final int freeCount;
	private final int acornCost;

	public TutorService(TermService termService, TutorUsageService usageService, AcornService acornService,
		TutorClient tutorClient,
		@Value("${tutor.free-count:3}") int freeCount,
		@Value("${tutor.acorn-cost:1}") int acornCost) {
		this.termService = termService;
		this.usageService = usageService;
		this.acornService = acornService;
		this.tutorClient = tutorClient;
		this.freeCount = freeCount;
		this.acornCost = acornCost;
	}

	public TutorResponse ask(Long userId, Long termId, String question) {
		Term term = termService.getTerm(termId);
		LocalDate today = LocalDate.now(KstTime.ZONE);

		boolean paid = usageService.getCount(userId, today) >= freeCount;
		if (paid && acornService.getBalance(userId) < acornCost) {
			throw new BusinessException(ErrorCode.ACORN_NOT_ENOUGH);
		}

		String answer = tutorClient.ask(term, question.trim());

		TutorUsageService.Recorded recorded = usageService.record(userId, today, freeCount, acornCost);
		int freeRemaining = Math.max(0, freeCount - recorded.countAfter());
		return new TutorResponse(answer, freeRemaining, recorded.acornSpent(), recorded.acornBalance());
	}
}
