package com.dotorich.question.seed;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.domain.QuestionType;

/** 시드 한 줄이 DB 에 넣을 수 있는 모양인지 검사한다. 문제가 있으면 이유 목록을 돌려준다 (없으면 빈 목록). */
public final class SeedValidator {

	private SeedValidator() {
	}

	public static List<String> checkTerm(SeedTerm t) {
		List<String> problems = new ArrayList<>();
		if (t.id() == null) {
			problems.add("id 없음");
		}
		if (isBlank(t.name()) || t.name().length() > 50) {
			problems.add("name 이 비었거나 50자 초과");
		}
		if (isBlank(t.definition())) {
			problems.add("definition 없음");
		}
		if (isBlank(t.easy())) {
			problems.add("easy 없음");
		}
		if (t.source() != null && t.source().length() > 100) {
			problems.add("source 100자 초과");
		}
		return problems;
	}

	/**
	 * @param termIds 넣을 수 있는 용어 id (시드 + DB 에 이미 있는 것)
	 * @param stageId 섹터·단계로 찾은 단계 id (시사 퀴즈가 아니면 null 이면 안 된다)
	 */
	public static List<String> checkQuestion(SeedQuestion q, Set<Long> termIds, Long stageId) {
		List<String> problems = new ArrayList<>();
		if (q.id() == null) {
			problems.add("id 없음");
		}
		QuestionPurpose purpose = enumOf(QuestionPurpose.class, q.purpose());
		QuestionType type = enumOf(QuestionType.class, q.type());
		if (purpose == null) {
			problems.add("purpose 가 LEARNING/LEVEL_TEST/NEWS 가 아님: " + q.purpose());
		}
		if (type == null) {
			problems.add("type 이 OX/MULTIPLE/MATCHING/SITUATION 이 아님: " + q.type());
		}
		if (purpose != QuestionPurpose.NEWS && stageId == null) {
			problems.add("sector·stage 에 맞는 단계 없음: " + q.sector() + "-" + q.stage());
		}
		if (purpose == QuestionPurpose.LEVEL_TEST && type == QuestionType.MATCHING) {
			problems.add("레벨 테스트에는 MATCHING 을 쓰지 않음");
		}
		if (purpose == QuestionPurpose.NEWS) {
			if (type == QuestionType.MATCHING) {
				problems.add("시사 퀴즈에는 MATCHING 을 쓰지 않음");
			}
			if (parseDate(q.publishDate()) == null) {
				problems.add("시사 퀴즈는 publishDate(yyyy-MM-dd)가 필요함: " + q.publishDate());
			}
		}
		if (isBlank(q.prompt()) || isBlank(q.explanation())) {
			problems.add("prompt 또는 explanation 없음");
		}
		if (q.source() != null && q.source().length() > 100) {
			problems.add("source 100자 초과");
		}
		for (Long termId : q.termIds() == null ? List.<Long>of() : q.termIds()) {
			if (!termIds.contains(termId)) {
				problems.add("없는 용어 id: " + termId);
			}
		}
		if (type != null) {
			problems.addAll(checkAnswer(type, q.options(), q.answer()));
		}
		return problems;
	}

	private static List<String> checkAnswer(QuestionType type, Object options, Object answer) {
		List<String> problems = new ArrayList<>();
		switch (type) {
			case OX -> {
				if (normalizeOx(answer) == null) {
					problems.add("OX 정답은 true/false 또는 \"O\"/\"X\"");
				}
			}
			case MULTIPLE, SITUATION -> {
				if (!(options instanceof List<?> list) || list.size() < 2) {
					problems.add("보기(options)가 2개 이상인 목록이어야 함");
				} else if (!(answer instanceof Number n) || n.intValue() < 0 || n.intValue() >= list.size()) {
					problems.add("정답은 보기 번호 0~" + (list.size() - 1));
				}
			}
			case MATCHING -> {
				if (!(options instanceof Map<?, ?> opt) || !(opt.get("terms") instanceof Collection<?> terms)
					|| !(opt.get("meanings") instanceof Collection<?> meanings)) {
					problems.add("매칭 보기는 {\"terms\": [...], \"meanings\": [...]}");
				} else if (!(answer instanceof Map<?, ?> ans) || !Set.copyOf(terms).equals(ans.keySet())
					|| !Set.copyOf(meanings).equals(Set.copyOf(ans.values()))) {
					problems.add("매칭 정답이 보기의 용어·뜻과 맞지 않음");
				}
			}
		}
		return problems;
	}

	/** OX 정답을 true/false 로 맞춘다. 알 수 없으면 null */
	public static Boolean normalizeOx(Object answer) {
		if (answer instanceof Boolean b) {
			return b;
		}
		if (answer instanceof String s) {
			String v = s.trim().toUpperCase();
			if (v.equals("O") || v.equals("TRUE")) {
				return true;
			}
			if (v.equals("X") || v.equals("FALSE")) {
				return false;
			}
		}
		return null;
	}

	/** "yyyy-MM-dd" 를 날짜로. 비었거나 형식이 틀리면 null */
	public static LocalDate parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(value.trim());
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private static <E extends Enum<E>> E enumOf(Class<E> type, String value) {
		if (value == null) {
			return null;
		}
		try {
			return Enum.valueOf(type, value.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}
}
