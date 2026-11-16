package com.dotorich.streak.domain;

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

/** study_days (P2). 하루 학습한 날. (user_id, study_date) 가 PK 라서 하루 1줄만 생긴다. */
@Getter
@Entity
@Table(name = "study_days")
@IdClass(StudyDay.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyDay {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "study_date")
	private LocalDate studyDate;

	public StudyDay(Long userId, LocalDate studyDate) {
		this.userId = userId;
		this.studyDate = studyDate;
	}

	@EqualsAndHashCode
	@NoArgsConstructor
	public static class Key implements Serializable {
		private Long userId;
		private LocalDate studyDate;
	}
}
