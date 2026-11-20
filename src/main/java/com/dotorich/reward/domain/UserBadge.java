package com.dotorich.reward.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** user_badges (P1). 지급 호출은 Part 2 가 BadgeService.grant 로 한다. */
@Getter
@Entity
@Table(name = "user_badges")
@IdClass(UserBadge.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "badge_id")
	private Long badgeId;

	@CreationTimestamp
	@Column(name = "earned_at", nullable = false, updatable = false)
	private LocalDateTime earnedAt;

	public UserBadge(Long userId, Long badgeId) {
		this.userId = userId;
		this.badgeId = badgeId;
	}

	@EqualsAndHashCode
	@NoArgsConstructor
	public static class Key implements Serializable {
		private Long userId;
		private Long badgeId;
	}
}
