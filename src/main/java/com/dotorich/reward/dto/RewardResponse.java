package com.dotorich.reward.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record RewardResponse(
	int acornBalance,
	List<AcornHistory> acornHistory,
	List<BadgeItem> badges
) {

	public record AcornHistory(int amount, String reason, OffsetDateTime createdAt) {
	}

	public record BadgeItem(Long badgeId, String name, String imageUrl, OffsetDateTime earnedAt) {
	}
}
