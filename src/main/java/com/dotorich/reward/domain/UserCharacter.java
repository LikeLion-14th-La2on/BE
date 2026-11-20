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

/** user_characters (P1). 보유 캐릭터 */
@Getter
@Entity
@Table(name = "user_characters")
@IdClass(UserCharacter.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCharacter {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "character_id")
	private Long characterId;

	@CreationTimestamp
	@Column(name = "purchased_at", nullable = false, updatable = false)
	private LocalDateTime purchasedAt;

	public UserCharacter(Long userId, Long characterId) {
		this.userId = userId;
		this.characterId = characterId;
	}

	@EqualsAndHashCode
	@NoArgsConstructor
	public static class Key implements Serializable {
		private Long userId;
		private Long characterId;
	}
}
