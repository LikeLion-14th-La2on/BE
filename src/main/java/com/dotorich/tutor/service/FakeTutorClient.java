package com.dotorich.tutor.service;

import com.dotorich.term.domain.Term;

/**
 * gemini.api-key 가 없을 때 쓰는 개발용 가짜 튜터.
 * 무료 한도를 아끼면서 흐름(횟수·도토리 차감)을 테스트할 수 있다.
 */
public class FakeTutorClient implements TutorClient {

	@Override
	public String ask(Term term, String question) {
		return "[개발용 가짜 답변] " + term.getName() + "은(는) " + term.getEasyExplanation()
			+ "이에요. (질문: " + question + ")";
	}
}
