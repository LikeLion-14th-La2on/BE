package com.dotorich.tutor.domain;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** tutor_usage (P3). 사용자별 하루(KST) AI 튜터 질문 수. 무료 횟수 판정에 쓴다. */
@Getter
@Entity
@Table(name = "tutor_usage")
@IdClass(TutorUsage.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TutorUsage {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "usage_date")
	private LocalDate usageDate;

	@Column(nullable = false)
	private int count;

	/** 테스트용 */
	public TutorUsage(Long userId, LocalDate usageDate, int count) {
		this.userId = userId;
		this.usageDate = usageDate;
		this.count = count;
	}

	public void increase() {
		this.count++;
	}

	@EqualsAndHashCode
	@NoArgsConstructor
	public static class Key implements Serializable {
		private Long userId;
		private LocalDate usageDate;
	}
}
