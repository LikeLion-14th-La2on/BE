package com.dotorich.question.seed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * seed_questions.json 한 줄.
 * options: OX 는 null, 객관식·상황형은 ["보기", ...], 매칭은 {"terms": [...], "meanings": [...]}
 * answer: OX 는 true/false("O"/"X" 도 허용), 객관식·상황형은 보기 번호(0부터), 매칭은 {"용어": "뜻"}
 * publishDate: 시사 퀴즈(NEWS)만, 게시일 "yyyy-MM-dd" (하루에 하나)
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
	String source,
	String publishDate
) {
}
