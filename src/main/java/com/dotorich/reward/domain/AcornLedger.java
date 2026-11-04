package com.dotorich.reward.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** acorn_ledger (P1). (user_id, reason, target_id) 유니크로 같은 보상 중복 지급을 막는다. */
@Getter
@Entity
@Table(name = "acorn_ledger",
	uniqueConstraints = @UniqueConstraint(name = "uk_acorn_once", columnNames = {"user_id", "reason", "target_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcornLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private int amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AcornReason reason;

	@Column(name = "target_id")
	private Long targetId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public AcornLedger(Long userId, int amount, AcornReason reason, Long targetId) {
		this.userId = userId;
		this.amount = amount;
		this.reason = reason;
		this.targetId = targetId;
	}
}
