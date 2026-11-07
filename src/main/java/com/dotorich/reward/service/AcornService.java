package com.dotorich.reward.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.reward.domain.AcornLedger;
import com.dotorich.reward.domain.AcornReason;
import com.dotorich.reward.repository.AcornLedgerRepository;
import com.dotorich.user.domain.User;
import com.dotorich.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 도토리 증감 함수. 다른 파트는 잔액을 직접 바꾸지 말고 이 클래스만 호출한다.
 * 호출하는 쪽 트랜잭션에 함께 묶인다 (예: 캐릭터 구매 = 차감 + 보유 저장이 한 트랜잭션).
 *
 * - earn: 적립. 같은 (reason, targetId) 는 한 번만 (예: 단계 최초 완수, 나무 완성)
 * - spend: 사용. 잔액이 부족하면 ACORN_NOT_ENOUGH
 * - penalizeStreakBreak: 연속 학습 끊김 -1. 잔액 0이면 차감 없음, 같은 날짜는 한 번만
 *
 * 반환값: 실제로 반영됐으면 true, 이미 처리됐거나 차감할 잔액이 없으면 false
 */
@Service
@RequiredArgsConstructor
public class AcornService {

	public static final int TREE_COMPLETE_BONUS = 5;
	public static final int STREAK_BREAK_PENALTY = 1;

	private final UserRepository userRepository;
	private final AcornLedgerRepository ledgerRepository;

	@Transactional
	public boolean earn(Long userId, int amount, AcornReason reason, Long targetId) {
		if (amount <= 0) {
			throw new IllegalArgumentException("적립 도토리는 1 이상이어야 합니다");
		}
		User user = lockUser(userId);
		if (alreadyDone(userId, reason, targetId)) {
			return false;
		}
		apply(user, amount, reason, targetId);
		return true;
	}

	@Transactional
	public boolean spend(Long userId, int amount, AcornReason reason, Long targetId) {
		if (amount < 0) {
			throw new IllegalArgumentException("사용 도토리는 0 이상이어야 합니다");
		}
		User user = lockUser(userId);
		if (alreadyDone(userId, reason, targetId)) {
			return false;
		}
		if (user.getAcornBalance() < amount) {
			throw new BusinessException(ErrorCode.ACORN_NOT_ENOUGH);
		}
		apply(user, -amount, reason, targetId);
		return true;
	}

	/** breakDate: 끊긴 날짜 yyyymmdd (예: 20261104). 6차 회의 규칙: 끊김 1회당 1개, 잔액 0이면 차감 없음 */
	@Transactional
	public boolean penalizeStreakBreak(Long userId, long breakDate) {
		User user = lockUser(userId);
		if (user.getAcornBalance() < STREAK_BREAK_PENALTY
			|| alreadyDone(userId, AcornReason.STREAK_BREAK, breakDate)) {
			return false;
		}
		apply(user, -STREAK_BREAK_PENALTY, AcornReason.STREAK_BREAK, breakDate);
		return true;
	}

	@Transactional(readOnly = true)
	public int getBalance(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
			.getAcornBalance();
	}

	private User lockUser(Long userId) {
		return userRepository.findByIdForUpdate(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	// target_id 가 null 인 사유(AI 튜터, 보호권)는 여러 번 허용
	private boolean alreadyDone(Long userId, AcornReason reason, Long targetId) {
		return targetId != null && ledgerRepository.existsByUserIdAndReasonAndTargetId(userId, reason, targetId);
	}

	private void apply(User user, int amount, AcornReason reason, Long targetId) {
		user.addAcorn(amount);
		ledgerRepository.save(new AcornLedger(user.getId(), amount, reason, targetId));
	}
}
