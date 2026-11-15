package com.dotorich.learning.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * user_stage_progress (P2). 사용자별 단계 상태. (user_id, stage_id) 가 PK.
 * 행이 없으면 잠긴 단계다. 단계 상태는 Part 2 에서만 저장한다.
 */
@Getter
@Entity
@Table(name = "user_stage_progress")
@IdClass(UserStageProgress.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStageProgress {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "stage_id")
	private Long stageId;

	/** UNLOCKED / PASSED_BY_TEST / COMPLETED */
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private StageStatus status;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	public UserStageProgress(Long userId, Long stageId, StageStatus status) {
		this.userId = userId;
		this.stageId = stageId;
		this.status = status;
	}

	@EqualsAndHashCode
	@NoArgsConstructor
	public static class Key implements Serializable {
		private Long userId;
		private Long stageId;
	}
}
