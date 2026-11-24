package com.dotorich.question.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class SeedLoaderParseTest {

	private static ByteArrayInputStream json(String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void 형식이_틀린_줄은_건너뛰고_나머지는_읽는다() {
		// id=2 는 related 자리에 문자열이 들어간, 실제로 서버 시작을 막았던 형식
		String body = """
			[{"id":1,"name":"금리","related":[]},
			 {"id":2,"name":"복리","related":""},
			 {"id":3,"name":"단리","related":["복리"]}]
			""";
		List<SeedTerm> rows = SeedLoader.parseRows(JsonMapper.builder().build(), json(body), SeedTerm.class, "t");
		assertEquals(List.of(1L, 3L), rows.stream().map(SeedTerm::id).toList());
	}

	@Test
	void 파일이_배열이_아니면_실패() {
		assertThrows(IllegalStateException.class,
			() -> SeedLoader.parseRows(JsonMapper.builder().build(), json("{}"), SeedTerm.class, "t"));
	}
}
