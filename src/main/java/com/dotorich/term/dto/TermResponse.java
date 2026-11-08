package com.dotorich.term.dto;

import com.dotorich.term.domain.Term;

/** 용어 조회 (뜻 팝업) */
public record TermResponse(
	Long termId,
	String name,
	String definition,
	String easyExplanation,
	String example,
	String reference,
	boolean bookmarked
) {

	public static TermResponse of(Term term, boolean bookmarked) {
		return new TermResponse(term.getId(), term.getName(), term.getDefinition(), term.getEasyExplanation(),
			term.getExample(), term.getReference(), bookmarked);
	}
}
