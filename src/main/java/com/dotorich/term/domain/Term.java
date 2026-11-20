package com.dotorich.term.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * terms (P3). 용어사전·뜻 팝업·AI 튜터가 공용으로 쓰는 용어.
 * 데이터는 시드(data.sql)로만 넣고, API 로 수정하지 않는다.
 */
@Getter
@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50, unique = true)
	private String name;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String definition;

	@Column(name = "easy_explanation", nullable = false, columnDefinition = "TEXT")
	private String easyExplanation;

	@Column(columnDefinition = "TEXT")
	private String example;

	@Column(length = 100)
	private String reference;

	/** 테스트용 */
	public Term(Long id, String name, String definition, String easyExplanation, String example, String reference) {
		this.id = id;
		this.name = name;
		this.definition = definition;
		this.easyExplanation = easyExplanation;
		this.example = example;
		this.reference = reference;
	}
}
