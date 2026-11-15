package com.dotorich.review.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

/**
 * wrong_answers (P2). 사용자별 오답 기록. (user_id, question_id) 는 한 줄만 있다.
 * 같은 문제를 또 틀리면 새로 만들지 않고 이 줄을 고친다.
 */
@Getter
@Entity
@Table(name = "wrong_answers",
		uniqueConstraints = @UniqueConstraint(name = "uk_wrong", columnNames = {"user_id", "question_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WrongAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "question_id", nullable = false)
	private Long questionId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private WrongAnswerStatus status;

	/** 사용자가 고른 답. 답안 제출 형식 그대로 ("O", "1", {"용어": "뜻"}) */
	@Column(name = "my_answer", columnDefinition = "TEXT")
	private String myAnswer;

	/** 주차별 조회 기준. 오답노트로 옮겨질 때 그 시각으로 바뀐다 */
	@Column(name = "wrong_at", nullable = false)
	private LocalDateTime wrongAt;

	/** 재출제에서 틀림 → 카드뉴스 복습 대기 */
	public static WrongAnswer forCardReview(Long userId, Long questionId, String myAnswer, LocalDateTime now) {
		WrongAnswer wrongAnswer = new WrongAnswer();
		wrongAnswer.userId = userId;
		wrongAnswer.questionId = questionId;
		wrongAnswer.status = WrongAnswerStatus.CARD_REVIEW;
		wrongAnswer.myAnswer = myAnswer;
		wrongAnswer.wrongAt = now;
		return wrongAnswer;
	}

	/**
	 * 재출제에서 또 틀림.
	 * 이미 복습 대기·오답노트면 고른 답만 고치고, 복습에서 맞혔던 문제(RESOLVED)면 다시 복습 대기로.
	 */
	public void wrongAgainInRetry(String myAnswer, LocalDateTime now) {
		this.myAnswer = myAnswer;
		if (status == WrongAnswerStatus.RESOLVED) {
			this.status = WrongAnswerStatus.CARD_REVIEW;
			this.wrongAt = now;
		}
	}

	/** 카드뉴스 복습에서 틀림 → 오답노트 */
	public void moveToNote(String myAnswer, LocalDateTime now) {
		this.status = WrongAnswerStatus.IN_NOTE;
		this.myAnswer = myAnswer;
		this.wrongAt = now;
	}

	/** 카드뉴스 복습에서 맞힘 */
	public void resolve() {
		this.status = WrongAnswerStatus.RESOLVED;
	}
}
