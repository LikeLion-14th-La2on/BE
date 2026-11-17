package com.dotorich.question.seed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * seed_questions.json 한 줄.
 * options: OX 는 null, 객관식·상황형은 ["보기", ...], 매칭은 {"terms": [...], "meanings": [...]}
 * answer: OX 는 true/false("O"/"X" 도 허용), 객관식·상황형은 보기 번호(0부터), 매칭은 {"용어": "뜻"}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SeedQuestion(
	Long id,
	String code,
	String purpose,
	Integer sector,
	Integer stage,
	String type,
	List<Long> termIds,
	String prompt,
	Object options,
	Object answer,
	String explanation,
	String source
) {
}
