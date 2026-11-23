package com.dotorich.tutor.service;

import java.util.List;

import com.dotorich.term.domain.Term;

/**
 * LLM 호출 창구. 용어 DB 설명 안에서만 답하게 한다.
 * related 는 질문에 함께 나온 다른 용어 (비교 질문용, 없으면 빈 리스트)
 * 구현: GeminiTutorClient (gemini.api-key 가 있을 때), FakeTutorClient (키가 없을 때, 개발용)
 * 실패하면 BusinessException(TUTOR_UNAVAILABLE) 을 던진다.
 */
public interface TutorClient {

	String ask(Term term, List<Term> related, String question);
}
