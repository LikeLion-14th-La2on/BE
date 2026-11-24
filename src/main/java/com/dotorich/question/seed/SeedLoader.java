package com.dotorich.question.seed;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.domain.QuestionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 문항·용어 시드 로더 (P3). 서버가 켜질 때 resources/seed 의 JSON 두 개를 DB 에 넣는다.
 *
 * - seed_terms.json → terms, seed_questions.json → questions, question_terms
 * - 시사 퀴즈(purpose = NEWS)는 publishDate 로 news_quizzes 에도 넣는다 (같은 날짜면 문제를 바꾼다)
 * - 같은 id 는 덮어쓴다. 검수 후 JSON 을 고쳐 다시 배포하면 수정 내용이 반영된다.
 * - 형식이 맞지 않는 줄은 건너뛰고 ERROR 로그에 id 와 이유를 남긴다 (나머지는 그대로 들어간다).
 * - JSON 에서 빠진 id 는 지우지 않는다 (오답 기록이 참조할 수 있어서). 로그로만 알린다.
 * - 단계 id 는 stages 테이블에서 (sector_id, step_no) 로 찾는다 (Part 2 시드: 주식 1~3, 생활 금융 4~6, 금융 기초 7~9).
 *
 * 끄려면 seed.enabled=false
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedLoader implements ApplicationRunner {

	static final String TERMS = "seed/seed_terms.json";
	static final String QUESTIONS = "seed/seed_questions.json";

	private final ObjectMapper objectMapper;
	private final JdbcTemplate jdbc;

	@Value("${seed.enabled:true}")
	private boolean enabled;

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws IOException {
		if (!enabled) {
			log.info("[시드] seed.enabled=false 라 건너뜀");
			return;
		}
		List<SeedTerm> terms = read(TERMS, SeedTerm.class);
		List<SeedQuestion> questions = read(QUESTIONS, SeedQuestion.class);
		if (terms.isEmpty() && questions.isEmpty()) {
			log.info("[시드] {} / {} 없음, 건너뜀", TERMS, QUESTIONS);
			return;
		}

		int termCount = upsertTerms(terms);
		int questionCount = upsertQuestions(questions);
		log.info("[시드] 용어 {}/{}개, 문제 {}/{}개 반영", termCount, terms.size(), questionCount, questions.size());
		warnMissing("terms", terms.stream().map(SeedTerm::id).toList());
		warnMissing("questions", questions.stream().map(SeedQuestion::id).toList());
	}

	private int upsertTerms(List<SeedTerm> terms) {
		int count = 0;
		for (SeedTerm t : terms) {
			List<String> problems = SeedValidator.checkTerm(t);
			if (!problems.isEmpty()) {
				log.error("[시드] 용어 id={} 건너뜀: {}", t.id(), problems);
				continue;
			}
			jdbc.update("""
				insert into terms (id, name, definition, easy_explanation, example, reference)
				values (?, ?, ?, ?, ?, ?) as new
				on duplicate key update name = new.name, definition = new.definition,
					easy_explanation = new.easy_explanation, example = new.example, reference = new.reference
				""", t.id(), t.name(), t.definition(), t.easy(), t.example(), t.source());
			count++;
		}
		return count;
	}

	private int upsertQuestions(List<SeedQuestion> questions) {
		Set<Long> termIds = new HashSet<>(jdbc.queryForList("select id from terms", Long.class));
		Map<String, Long> stageIds = new HashMap<>();
		jdbc.query("select id, sector_id, step_no from stages",
			rs -> {
				stageIds.put(rs.getLong("sector_id") + "-" + rs.getInt("step_no"), rs.getLong("id"));
			});

		int count = 0;
		for (SeedQuestion q : questions) {
			Long stageId = stageIds.get(q.sector() + "-" + q.stage());
			List<String> problems = SeedValidator.checkQuestion(q, termIds, stageId);
			if (!problems.isEmpty()) {
				log.error("[시드] 문제 id={} ({}) 건너뜀: {}", q.id(), q.code(), problems);
				continue;
			}
			Object answer = QuestionType.OX.name().equals(q.type().trim())
				? SeedValidator.normalizeOx(q.answer()) : q.answer();
			jdbc.update("""
				insert into questions (id, stage_id, purpose, type, content, choices, answer, explanation, reference)
				values (?, ?, ?, ?, ?, ?, ?, ?, ?) as new
				on duplicate key update stage_id = new.stage_id, purpose = new.purpose, type = new.type,
					content = new.content, choices = new.choices, answer = new.answer,
					explanation = new.explanation, reference = new.reference
				""", q.id(), stageId, q.purpose().trim(), q.type().trim(), q.prompt(),
				q.options() == null ? null : objectMapper.writeValueAsString(q.options()),
				objectMapper.writeValueAsString(answer), q.explanation(), q.source());

			jdbc.update("delete from question_terms where question_id = ?", q.id());
			for (Long termId : q.termIds() == null ? List.<Long>of() : q.termIds()) {
				jdbc.update("insert into question_terms (question_id, term_id) values (?, ?)", q.id(), termId);
			}
			if (QuestionPurpose.NEWS.name().equals(q.purpose().trim())) {
				upsertNewsQuiz(q.id(), SeedValidator.parseDate(q.publishDate()));
			}
			count++;
		}
		return count;
	}

	/**
	 * 게시일 기준으로 넣는다. quizId(news_quizzes.id)가 배포할 때마다 바뀌지 않도록 지우고 다시 넣지 않는다.
	 * 문제의 게시일이 바뀌었으면 예전 날짜 줄만 지운다.
	 */
	private void upsertNewsQuiz(Long questionId, LocalDate publishDate) {
		jdbc.update("delete from news_quizzes where question_id = ? and publish_date <> ?", questionId, publishDate);
		jdbc.update("""
			insert into news_quizzes (question_id, publish_date) values (?, ?) as new
			on duplicate key update question_id = new.question_id
			""", questionId, publishDate);
	}

	/** DB 에는 있는데 JSON 에서 빠진 id 를 알린다 (지우지는 않는다) */
	private void warnMissing(String table, List<Long> seedIds) {
		List<Long> missing = jdbc.queryForList("select id from " + table, Long.class).stream()
			.filter(id -> !seedIds.contains(id))
			.toList();
		if (!missing.isEmpty()) {
			log.warn("[시드] {} 에는 있는데 시드 JSON 에 없는 id: {}", table, missing);
		}
	}

	private <T> List<T> read(String path, Class<T> type) throws IOException {
		ClassPathResource resource = new ClassPathResource(path);
		if (!resource.exists()) {
			return List.of();
		}
		try (InputStream in = resource.getInputStream()) {
			return parseRows(objectMapper, in, type, path);
		}
	}

	/**
	 * JSON 배열을 한 줄씩 읽는다. 형식이 틀린 줄(예: 목록 자리에 문자열)은 건너뛰고 ERROR 로그만 남겨서,
	 * 한 줄 때문에 서버 시작이 실패하지 않게 한다 (#24). 파일 자체가 배열이 아니면 그대로 실패한다.
	 */
	static <T> List<T> parseRows(ObjectMapper objectMapper, InputStream in, Class<T> type, String path) {
		JsonNode root = objectMapper.readTree(in);
		if (!root.isArray()) {
			throw new IllegalStateException(path + " 는 JSON 배열이어야 합니다");
		}
		List<T> rows = new ArrayList<>();
		for (JsonNode row : root) {
			try {
				rows.add(objectMapper.treeToValue(row, type));
			} catch (JacksonException e) {
				log.error("[시드] {} id={} 줄을 읽지 못해 건너뜀: {}", path, row.path("id"), e.getOriginalMessage());
			}
		}
		return rows;
	}
}
