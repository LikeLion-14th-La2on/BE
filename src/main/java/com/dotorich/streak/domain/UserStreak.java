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

	/**
	 * 하루 학습 기록. 어제 학습했으면 +1, 오늘 이미 했으면 그대로, 그 외(처음·끊김)는 1부터.
	 * 보호권으로 끊김을 막은 경우는 끊김 판정에서 마지막 학습일을 어제로 맞춰 둔다.
	 */
	public void recordStudy(LocalDate today) {
		if (today.equals(lastStudyDate)) {
			return;
		}
		boolean continued = lastStudyDate != null && lastStudyDate.plusDays(1).equals(today);
		currentCount = continued ? currentCount + 1 : 1;
		lastStudyDate = today;
	}
}
