package com.dotorich.question.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** question_terms (P3). 문제별 연결 용어 (용어 카드, 해설의 "AI에게 물어보기"에 쓴다) */
@Getter
@Entity
@Table(name = "question_terms")
@IdClass(QuestionTerm.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionTerm {

	@Id
	@Column(name = "question_id")
	private Long questionId;

	@Id
	@Column(name = "term_id")
	private Long termId;

	public QuestionTerm(Long questionId, Long termId) {
		this.questionId = questionId;
		this.termId = termId;
	}

	@EqualsAndHashCode
	@NoArgsConstructor
	public static class Key implements Serializable {
		private Long questionId;
		private Long termId;
	}
}
