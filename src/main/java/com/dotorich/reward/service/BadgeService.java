package com.dotorich.reward.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.reward.domain.UserBadge;
import com.dotorich.reward.repository.BadgeRepository;
import com.dotorich.reward.repository.UserBadgeRepository;

import lombok.RequiredArgsConstructor;

/** 배지 지급 함수. Part 2 가 조건을 판단해 grant 를 호출한다. 이미 있으면 false */
@Service
@RequiredArgsConstructor
public class BadgeService {

	private final BadgeRepository badgeRepository;
	private final UserBadgeRepository userBadgeRepository;

	@Transactional
	public boolean grant(Long userId, Long badgeId) {
		if (!badgeRepository.existsById(badgeId)) {
			throw new BusinessException(ErrorCode.NOT_FOUND);
		}
		if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
			return false;
		}
		userBadgeRepository.save(new UserBadge(userId, badgeId));
		return true;
	}
}
