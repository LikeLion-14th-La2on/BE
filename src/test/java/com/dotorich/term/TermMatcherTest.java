package com.dotorich.term;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.dotorich.term.domain.Term;
import com.dotorich.term.service.TermMatcher;

class TermMatcherTest {

	private final Term 금리 = term(1L, "금리");
	private final Term 원금균등 = term(9L, "원금균등상환");
	private final Term 원리금균등 = term(10L, "원리금균등상환");
	private final Term per = term(14L, "PER");
	private final Term eps = term(18L, "주당순이익(EPS)");
	private final Term 명목실질 = term(37L, "명목금리/실질금리");
	private final List<Term> all = List.of(금리, 원금균등, 원리금균등, per, eps, 명목실질);

	@Test
	void 질문에_나온_다른_용어를_찾고_보고_있는_용어는_뺀다() {
		List<Term> found = TermMatcher.findMentioned(all, "원금균등상환이랑 뭐가 달라요?", 10L, 3);

		assertEquals(List.of(원금균등), found);
	}

	@Test
	void 긴_이름_안의_짧은_이름은_따로_잡지_않는다() {
		List<Term> found = TermMatcher.findMentioned(all, "원리금균등상환은 원금균등상환보다 이자가 많아요?", 10L, 3);

		assertEquals(List.of(원금균등), found);
	}

	@Test
	void 띄어쓰기와_대소문자를_무시한다() {
		List<Term> found = TermMatcher.findMentioned(all, "원금 균등 상환이랑 per 차이", 10L, 3);

		assertEquals(2, found.size());
		assertTrue(found.containsAll(List.of(원금균등, per)));
	}

	@Test
	void 괄호_안_약어와_슬래시로_나뉜_이름으로도_찾는다() {
		List<Term> found = TermMatcher.findMentioned(all, "EPS랑 실질금리는 어떤 관계예요?", 1L, 3);

		assertEquals(2, found.size());
		assertTrue(found.containsAll(List.of(eps, 명목실질)));
	}

	@Test
	void 최대_개수까지만_돌려준다() {
		List<Term> found = TermMatcher.findMentioned(all, "금리, PER, EPS, 원금균등상환 다 알려줘", 10L, 2);

		assertEquals(2, found.size());
	}

	@Test
	void 아무_용어도_없으면_빈_리스트() {
		assertTrue(TermMatcher.findMentioned(all, "이거 왜 이래요?", 10L, 3).isEmpty());
	}

	private static Term term(Long id, String name) {
		return new Term(id, name, name + " 뜻", name + " 쉬운 설명", null, null);
	}
}
