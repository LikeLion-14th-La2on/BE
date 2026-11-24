package com.dotorich.question.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SeedValidatorTest {

	private static final Set<Long> TERMS = Set.of(1L, 2L, 3L);

	private static SeedQuestion q(String purpose, String type, Object options, Object answer, List<Long> termIds) {
		return new SeedQuestion(1L, "S1-1-1", purpose, 1, 1, type, termIds, "문제", options, answer, "해설", "출처", null);
	}

	@Test
	void 정상_문제는_통과() {
		assertTrue(SeedValidator.checkQuestion(q("LEARNING", "OX", null, true, List.of(1L)), TERMS, 1L).isEmpty());
		assertTrue(SeedValidator.checkQuestion(
			q("LEVEL_TEST", "MULTIPLE", List.of("a", "b", "c"), 2, List.of(2L)), TERMS, 1L).isEmpty());
		assertTrue(SeedValidator.checkQuestion(q("LEARNING", "MATCHING",
			Map.of("terms", List.of("A", "B"), "meanings", List.of("가", "나")),
			Map.of("A", "가", "B", "나"), List.of(1L, 2L)), TERMS, 1L).isEmpty());
	}

	@Test
	void 보기_번호가_범위를_벗어나면_실패() {
		List<String> problems = SeedValidator.checkQuestion(
			q("LEARNING", "MULTIPLE", List.of("a", "b"), 2, List.of()), TERMS, 1L);
		assertFalse(problems.isEmpty());
	}

	@Test
	void 없는_용어_단계_잘못된_유형은_실패() {
		assertFalse(SeedValidator.checkQuestion(q("LEARNING", "OX", null, true, List.of(99L)), TERMS, 1L).isEmpty());
		assertFalse(SeedValidator.checkQuestion(q("LEARNING", "OX", null, true, List.of()), TERMS, null).isEmpty());
		assertFalse(SeedValidator.checkQuestion(q("LEARNING", "ESSAY", null, true, List.of()), TERMS, 1L).isEmpty());
	}

	@Test
	void 레벨_테스트에_매칭은_실패() {
		List<String> problems = SeedValidator.checkQuestion(q("LEVEL_TEST", "MATCHING",
			Map.of("terms", List.of("A"), "meanings", List.of("가")), Map.of("A", "가"), List.of()), TERMS, 1L);
		assertFalse(problems.isEmpty());
	}

	@Test
	void 매칭_정답이_보기와_다르면_실패() {
		List<String> problems = SeedValidator.checkQuestion(q("LEARNING", "MATCHING",
			Map.of("terms", List.of("A", "B"), "meanings", List.of("가", "나")),
			Map.of("A", "가", "B", "다"), List.of()), TERMS, 1L);
		assertFalse(problems.isEmpty());
	}

	@Test
	void OX_정답은_O_X도_받는다() {
		assertEquals(true, SeedValidator.normalizeOx("O"));
		assertEquals(false, SeedValidator.normalizeOx("x"));
		assertEquals(false, SeedValidator.normalizeOx(false));
		assertNull(SeedValidator.normalizeOx(1));
	}

	@Test
	void 용어는_이름_뜻_쉬운설명이_필요() {
		assertTrue(SeedValidator.checkTerm(new SeedTerm(2L, "기준금리", 3, 1, "뜻", "쉬운 설명", null, List.of(), "출처")).isEmpty());
		assertFalse(SeedValidator.checkTerm(new SeedTerm(2L, "기준금리", 3, 1, "뜻", "", null, List.of(), "출처")).isEmpty());
	}

	@Test
	void 시사_퀴즈는_게시일이_있어야_하고_단계는_없어도_된다() {
		SeedQuestion ok = new SeedQuestion(70L, "N-1", "NEWS", null, null, "OX", List.of(), "문제", null, true, "해설",
			null, "2026-11-23");
		SeedQuestion noDate = new SeedQuestion(71L, "N-2", "NEWS", null, null, "OX", List.of(), "문제", null, true, "해설",
			null, null);
		SeedQuestion badDate = new SeedQuestion(72L, "N-3", "NEWS", null, null, "OX", List.of(), "문제", null, true, "해설",
			null, "11/23");

		assertTrue(SeedValidator.checkQuestion(ok, TERMS, null).isEmpty());
		assertFalse(SeedValidator.checkQuestion(noDate, TERMS, null).isEmpty());
		assertFalse(SeedValidator.checkQuestion(badDate, TERMS, null).isEmpty());
	}

	@Test
	void 보기_번호가_정수가_아니면_실패() {
		assertFalse(SeedValidator.checkQuestion(
			q("LEARNING", "MULTIPLE", List.of("a", "b", "c"), 1.5, List.of()), TERMS, 1L).isEmpty());
	}
}
