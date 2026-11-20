package com.dotorich.question.seed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** seed_terms.json 한 줄. sector·stage·related 는 시트 정리용이라 DB 에 넣지 않는다. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SeedTerm(
	Long id,
	String name,
	Integer sector,
	Integer stage,
	String definition,
	String easy,
	String example,
	List<String> related,
	String source
) {
}
