package com.dotorich.tutor.service;

import java.time.LocalDate;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.reward.domain.AcornReason;
import com.dotorich.reward.service.AcornService;
import com.dotorich.tutor.domain.TutorUsage;
import com.dotorich.tutor.repository.TutorUsageRepository;
import com.dotorich.user.domain.UserDeletedEvent;

import lombok.RequiredArgsConstructor;

/** AI 튜터 사용 횟수와 도토리 차감. TutorService 가 LLM 답변을 받은 뒤에만 record 를 부른다. */
@Service
@RequiredArgsConstructor
public class TutorUsageService {

	private final TutorUsageRepository usageRepository;
	private final AcornService acornService;

	@Transactional(readOnly = true)
	public int getCount(Long userId, LocalDate date) {
		return usageRepository.findByUserIdAndUsageDate(userId, date)
			.map(TutorUsage::getCount)
			.orElse(0);
	}

	/**
	 * 질문 1회 기록. 무료 횟수를 넘었으면 도토리 차감 (Part 1 AcornService, reason=AI_TUTOR).
	 * 잔액이 부족하면 ACORN_NOT_ENOUGH 로 횟수도 같이 롤백된다.
	 */
	@Transactional
	public Recorded record(Long userId, LocalDate date, int freeCount, int acornCost) {
		usageRepository.insertIgnore(userId, date);
		TutorUsage usage = usageRepository.findForUpdate(userId, date)
			.orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
		int spent = 0;
		if (usage.getCount() >= freeCount) {
			acornService.spend(userId, acornCost, AcornReason.AI_TUTOR, null);
			spent = acornCost;
		}
		usage.increase();
		return new Recorded(usage.getCount(), spent, acornService.getBalance(userId));
	}

	@EventListener
	public void onUserDeleted(UserDeletedEvent event) {
		usageRepository.deleteAllByUserId(event.userId());
	}

	/** countAfter: 이번 질문까지 포함한 오늘 횟수 */
	public record Recorded(int countAfter, int acornSpent, int acornBalance) {
	}
}
