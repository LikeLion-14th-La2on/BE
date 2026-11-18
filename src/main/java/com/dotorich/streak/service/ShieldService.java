package com.dotorich.streak.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.reward.domain.AcornReason;
import com.dotorich.reward.service.AcornService;
import com.dotorich.streak.domain.UserStreak;
import com.dotorich.streak.dto.ShieldPurchaseResponse;
import com.dotorich.streak.repository.UserStreakRepository;

import lombok.RequiredArgsConstructor;

/**
 * 스트릭 보호권 구매 (P2, 2순위). 도토리 5개로 1개, 최대 2개 (11/17 확정).
 * 1. 끊김 판정을 먼저 한다 → 이미 지난 끊김을 방금 산 보호권으로 막을 수 없다
 * 2. 이미 2개면 400 (SHIELD_LIMIT)
 * 3. 도토리 5개 차감 (Part 1, reason=STREAK_SHIELD). 부족하면 400 (ACORN_NOT_ENOUGH)
 * 4. 보호권 +1
 * 실패하면 같은 트랜잭션이라 1번의 끊김 처리도 취소되고, 다음 홈 조회 때 다시 처리된다.
 */
@Service
@RequiredArgsConstructor
public class ShieldService {

	/** 보호권 가격 (11/17 확정) */
	public static final int SHIELD_PRICE = 5;

	private final StreakBreakService streakBreakService;
	private final UserStreakRepository userStreakRepository;
	private final AcornService acornService;

	@Transactional
	public ShieldPurchaseResponse purchase(Long userId) {
		streakBreakService.check(userId, LocalDate.now(KstTime.ZONE));

		UserStreak streak = userStreakRepository.findByIdForUpdate(userId)
				.orElseGet(() -> userStreakRepository.save(new UserStreak(userId)));
		if (streak.isShieldFull()) {
			throw new BusinessException(ErrorCode.SHIELD_LIMIT);
		}

		acornService.spend(userId, SHIELD_PRICE, AcornReason.STREAK_SHIELD, null);
		streak.addShield();
		return new ShieldPurchaseResponse(streak.getShieldCount(), acornService.getBalance(userId));
	}
}
