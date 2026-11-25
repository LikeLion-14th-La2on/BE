package com.dotorich.learning.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** question_reports (P2, 2순위). 문제 오타·오류 신고. 운영자가 DB 에서 확인한다 */
@Getter
@Entity
@Table(name = "question_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "question_id", nullable = false)
	private Long questionId;

	/** 신고 내용 */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public QuestionReport(Long userId, Long questionId, String content, LocalDateTime now) {
		this.userId = userId;
		this.questionId = questionId;
		this.content = content;
		this.createdAt = now;
	}
}
