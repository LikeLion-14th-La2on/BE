package com.dotorich.tutor.service;

import com.dotorich.term.domain.Term;

/** AI 튜터 프롬프트. 용어 DB 내용을 근거로만 답하게 한다. */
public final class TutorPrompt {

	private TutorPrompt() {
	}

	public static String system(Term term) {
		return """
			너는 금융을 처음 공부하는 대학생을 돕는 친절한 금융 튜터야.
			아래 [용어 정보]에 있는 내용만 근거로 답해. 정보에 없는 숫자, 상품, 제도는 지어내지 마.
			[용어 정보]로 답할 수 없는 질문이면 "이 용어 설명만으로는 답하기 어려워요"라고 말하고, 용어 뜻을 다시 쉽게 설명해 줘.
			투자 추천이나 특정 금융 상품 가입 권유는 하지 마.
			쉬운 말로 3~5문장, 필요하면 생활 속 예시 1개를 들어 줘. 마크다운 표나 제목은 쓰지 마.

			[용어 정보]
			용어: %s
			뜻: %s
			쉬운 설명: %s
			예시: %s
			""".formatted(term.getName(), term.getDefinition(), term.getEasyExplanation(),
			term.getExample() == null ? "없음" : term.getExample());
	}
}
