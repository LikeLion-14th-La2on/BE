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

	/** 마지막으로 처리한 끊김 날짜 (같은 끊김을 두 번 처리하지 않도록) */
	@Column(name = "last_break_date")
	private LocalDate lastBreakDate;

	public UserStreak(Long userId) {
		this.userId = userId;
	}

	/** 그 날짜에 학습했는지 */
	public boolean studiedOn(LocalDate date) {
		return date.equals(lastStudyDate);
	}

	/**
	 * 하루 학습 기록. 어제 학습했으면 +1, 오늘 이미 했으면 그대로, 그 외(처음·끊김)는 1부터.
	 * 보호권으로 끊김을 막은 경우는 useShield 에서 마지막 학습일을 어제로 맞춰 둔다.
	 */
	public void recordStudy(LocalDate today) {
		if (today.equals(lastStudyDate)) {
			return;
		}
		boolean continued = lastStudyDate != null && lastStudyDate.plusDays(1).equals(today);
		currentCount = continued ? currentCount + 1 : 1;
		lastStudyDate = today;
	}

	/**
	 * 오늘 기준으로 처리하지 않은 끊김이 있으면 그 끊김 날짜(학습을 처음 빼먹은 날), 없으면 null.
	 * 마지막 학습일이 그저께 이전이면 끊김이다. 한 번도 학습하지 않았으면 끊김이 아니다.
	 */
	public LocalDate unhandledBreakDate(LocalDate today) {
		if (lastStudyDate == null || !lastStudyDate.isBefore(today.minusDays(1))) {
			return null;
		}
		LocalDate breakDate = lastStudyDate.plusDays(1);
		return breakDate.equals(lastBreakDate) ? null : breakDate;
	}

	public boolean hasShield() {
		return shieldCount > 0;
	}

	/** 보호권으로 끊김을 막는다. 연속 기록은 유지하고, 오늘 학습하면 +1 이 되도록 마지막 학습일을 어제로 맞춘다 */
	public void useShield(LocalDate today) {
		shieldCount--;
		lastStudyDate = today.minusDays(1);
	}

	/** 끊김 처리: 연속 기록 0, 처리한 끊김 날짜 기록 */
	public void breakStreak(LocalDate breakDate) {
		currentCount = 0;
		lastBreakDate = breakDate;
	}
}
