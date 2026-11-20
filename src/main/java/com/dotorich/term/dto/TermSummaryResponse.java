package com.dotorich.term.dto;

import com.dotorich.term.domain.Term;

/** 용어 검색 결과 한 줄 */
public record TermSummaryResponse(
	Long termId,
	String name,
	String easyExplanation
) {

	public static TermSummaryResponse of(Term term) {
		return new TermSummaryResponse(term.getId(), term.getName(), term.getEasyExplanation());
	}
}
