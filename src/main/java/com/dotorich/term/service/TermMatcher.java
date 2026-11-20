package com.dotorich.term.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.dotorich.term.domain.Term;

/**
 * 글(질문) 안에 나온 용어를 찾는다. AI 튜터가 비교 질문에 답할 수 있게 함께 넘길 용어를 고른다.
 *
 * - 띄어쓰기·대소문자는 무시한다 ("원리금 균등상환", "per" 도 찾음)
 * - "주당순이익(EPS)" 는 전체, "주당순이익", "EPS" 어느 것으로도 찾고, "명목금리/실질금리" 는 / 로 나눠 찾는다
 * - 긴 이름부터 찾고 찾은 부분은 지워서, "원리금균등상환" 안의 "금리" 같은 짧은 이름이 덩달아 잡히지 않게 한다
 */
public final class TermMatcher {

	private static final int MIN_KEY_LENGTH = 2;

	private TermMatcher() {
	}

	public static List<Term> findMentioned(List<Term> terms, String text, Long excludeId, int limit) {
		if (text == null || text.isBlank() || limit <= 0) {
			return List.of();
		}
		record Key(String value, Term term) {
		}
		List<Key> keys = new ArrayList<>();
		for (Term term : terms) {
			for (String key : keysOf(term.getName())) {
				keys.add(new Key(key, term));
			}
		}
		keys.sort(Comparator.comparingInt((Key k) -> k.value().length()).reversed());

		String rest = normalize(text);
		Set<Term> found = new LinkedHashSet<>();
		for (Key key : keys) {
			if (rest.contains(key.value())) {
				rest = rest.replace(key.value(), " ");
				if (!key.term().getId().equals(excludeId)) {
					found.add(key.term());
				}
			}
		}
		return found.stream().limit(limit).toList();
	}

	static Set<String> keysOf(String name) {
		Set<String> keys = new LinkedHashSet<>();
		String normalized = normalize(name);
		keys.add(normalized);
		for (String part : normalized.split("[()/]")) {
			keys.add(part);
		}
		keys.removeIf(k -> k.length() < MIN_KEY_LENGTH);
		return keys;
	}

	private static String normalize(String s) {
		return s.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
	}
}
