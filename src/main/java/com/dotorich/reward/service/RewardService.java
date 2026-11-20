package com.dotorich.reward.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.util.KstTime;
import com.dotorich.reward.domain.Badge;
import com.dotorich.reward.dto.RewardResponse;
import com.dotorich.reward.repository.AcornLedgerRepository;
import com.dotorich.reward.repository.BadgeRepository;
import com.dotorich.reward.repository.UserBadgeRepository;
import com.dotorich.reward.repository.UserCharacterRepository;
import com.dotorich.user.domain.UserDeletedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RewardService {

	private final AcornService acornService;
	private final AcornLedgerRepository ledgerRepository;
	private final BadgeRepository badgeRepository;
	private final UserBadgeRepository userBadgeRepository;
	private final UserCharacterRepository userCharacterRepository;

	@Transactional(readOnly = true)
	public RewardResponse getRewards(Long userId) {
		List<RewardResponse.AcornHistory> history = ledgerRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId)
			.stream()
			.map(l -> new RewardResponse.AcornHistory(l.getAmount(), l.getReason().name(), KstTime.of(l.getCreatedAt())))
			.toList();

		Map<Long, Badge> badges = badgeRepository.findAll().stream()
			.collect(Collectors.toMap(Badge::getId, Function.identity()));
		List<RewardResponse.BadgeItem> earned = userBadgeRepository.findAllByUserIdOrderByEarnedAtDesc(userId).stream()
			.filter(ub -> badges.containsKey(ub.getBadgeId()))
			.map(ub -> {
				Badge b = badges.get(ub.getBadgeId());
				return new RewardResponse.BadgeItem(b.getId(), b.getName(), b.getImageUrl(), KstTime.of(ub.getEarnedAt()));
			})
			.toList();

		return new RewardResponse(acornService.getBalance(userId), history, earned);
	}

	@EventListener
	public void onUserDeleted(UserDeletedEvent event) {
		ledgerRepository.deleteAllByUserId(event.userId());
		userCharacterRepository.deleteAllByUserId(event.userId());
		userBadgeRepository.deleteAllByUserId(event.userId());
	}
}
