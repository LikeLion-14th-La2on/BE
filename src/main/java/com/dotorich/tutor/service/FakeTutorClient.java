package com.dotorich.tutor.service;

import java.util.List;

import com.dotorich.term.domain.Term;

/**
 * gemini.api-key 가 없을 때 쓰는 개발용 가짜 튜터.
 * 무료 한도를 아끼면서 흐름(횟수·도토리 차감)을 테스트할 수 있다.
 */
public class FakeTutorClient implements TutorClient {

	@Override
	public String ask(Term term, List<Term> related, String question) {
		String others = related.isEmpty() ? ""
			: " (함께 나온 용어: " + String.join(", ", related.stream().map(Term::getName).toList()) + ")";
		return "[개발용 가짜 답변] " + term.getName() + "은(는) " + term.getEasyExplanation()
			+ "이에요." + others + " (질문: " + question + ")";
	}
}
