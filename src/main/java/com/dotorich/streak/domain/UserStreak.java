package com.dotorich.streak.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * user_streaks (P2). 사용자별 연속 학습 기록. 사용자당 1행.
 * 행이 없으면 아직 한 번도 학습하지 않은 사용자다.
 */
@Getter
@Entity
@Table(name = "user_streaks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStreak {

	@Id
	@Column(name = "user_id")
	private Long userId;

	/** 연속 학습일 */
	@Column(name = "current_count", nullable = false)
	private int currentCount;

	/** 마지막 학습일 (KST) */
	@Column(name = "last_study_date")
	private LocalDate lastStudyDate;

	/** 보유 보호권 */
	@Column(name = "shield_count", nullable = false)
	private int shieldCount;

	public UserStreak(Long userId) {
		this.userId = userId;
	}

	/** 그 날짜에 학습했는지 */
	public boolean studiedOn(LocalDate date) {
		return date.equals(lastStudyDate);
	}
}
