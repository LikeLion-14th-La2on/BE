package com.dotorich.question.domain;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * questions (P3). 학습·레벨 테스트·시사 퀴즈 공용 문제. 데이터는 시드로만 넣는다.
 * stage_id 는 Part 2 시드의 고정 번호(주식 1~3, 생활 금융 4~6, 금융 기초 7~9), 시사 퀴즈는 NULL.
 * choices, answer 는 JSON 문자열 그대로 저장한다. 모양은 QuestionType 주석 참고.
 */
@Getter
@Entity
@Table(name = "questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "stage_id")
	private Long stageId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR) // MySQL enum 칸이 아닌 varchar 로. enum 값을 추가해도 DB를 고칠 필요가 없다
	@Column(nullable = false, length = 20)
	private QuestionPurpose purpose;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR) // MySQL enum 칸이 아닌 varchar 로. enum 값을 추가해도 DB를 고칠 필요가 없다
	@Column(nullable = false, length = 20)
	private QuestionType type;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(columnDefinition = "json")
	private String choices;

	@Column(nullable = false, columnDefinition = "json")
	private String answer;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String explanation;

	@Column(length = 100)
	private String reference;

	/** 테스트용 */
	public Question(Long id, Long stageId, QuestionPurpose purpose, QuestionType type, String content,
		String choices, String answer, String explanation, String reference) {
		this.id = id;
		this.stageId = stageId;
		this.purpose = purpose;
		this.type = type;
		this.content = content;
		this.choices = choices;
		this.answer = answer;
		this.explanation = explanation;
		this.reference = reference;
	}
}
