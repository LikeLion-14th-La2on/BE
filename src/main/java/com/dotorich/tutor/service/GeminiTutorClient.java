package com.dotorich.tutor.service;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.term.domain.Term;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Gemini generateContent REST 호출.
 * https://ai.google.dev/gemini-api/docs/generate-content/text-generation
 * 최신 모델은 temperature 를 무시하고 나중에 400 을 줄 수 있다고 해서 넣지 않는다.
 * HTTP 클라이언트는 Java 기본 HttpClient (HTTP/2 지원). 네트워크 오류는 한 번 다시 시도한다.
 * 응답은 바이트로 받아 직접 JSON(UTF-8) 으로 읽는다 (Content-Type 이 이상해도 읽을 수 있고, 실패하면 원인을 로그로 남긴다).
 */
@Slf4j
public class GeminiTutorClient implements TutorClient {

	private static final int MAX_OUTPUT_TOKENS = 800;
	private static final int LOG_BODY_LIMIT = 300;
	private static final int MAX_ATTEMPTS = 2;

	private final RestClient restClient;
	private final String model;
	private final ObjectMapper objectMapper;

	public GeminiTutorClient(String apiKey, String model, ObjectMapper objectMapper) {
		HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(3))
			.build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(Duration.ofSeconds(20));
		this.restClient = RestClient.builder()
			.baseUrl("https://generativelanguage.googleapis.com")
			.defaultHeader("x-goog-api-key", apiKey)
			.requestFactory(factory)
			.build();
		this.model = model;
		this.objectMapper = objectMapper;
	}

	@Override
	public String ask(Term term, List<Term> related, String question) {
		Map<String, Object> body = Map.of(
			"systemInstruction", Map.of("parts", List.of(Map.of("text", TutorPrompt.system(term, related)))),
			"contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", question)))),
			"generationConfig", Map.of("maxOutputTokens", MAX_OUTPUT_TOKENS));
		ResponseEntity<byte[]> response = call(term, body);

		try {
			return extractText(objectMapper.readValue(response.getBody(), Map.class));
		} catch (JacksonException | IllegalArgumentException | ClassCastException | NullPointerException
			| IndexOutOfBoundsException e) {
			byte[] raw = response.getBody();
			log.warn("AI 튜터 응답 해석 실패 (termId={}): HTTP {}, Content-Type={}, body={}", term.getId(),
				response.getStatusCode(), response.getHeaders().getContentType(),
				abbreviate(raw == null ? null : new String(raw, StandardCharsets.UTF_8)));
			throw new BusinessException(ErrorCode.TUTOR_UNAVAILABLE);
		}
	}

	/** 네트워크 오류(연결 끊김 등)는 한 번만 다시 시도한다. HTTP 오류 응답(4xx, 5xx)은 다시 시도하지 않는다 */
	private ResponseEntity<byte[]> call(Term term, Map<String, Object> body) {
		for (int attempt = 1; ; attempt++) {
			try {
				return restClient.post()
					.uri("/v1beta/models/{model}:generateContent", model)
					.body(body)
					.retrieve()
					.toEntity(byte[].class);
			} catch (RestClientResponseException e) {
				log.warn("AI 튜터 호출 실패 (termId={}): HTTP {} {}", term.getId(), e.getStatusCode(),
					abbreviate(e.getResponseBodyAsString()));
				throw new BusinessException(ErrorCode.TUTOR_UNAVAILABLE);
			} catch (RestClientException e) {
				Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
				log.warn("AI 튜터 호출 실패 (termId={}, {}번째 시도): {} / 원인: {}: {}", term.getId(), attempt,
					e.getMessage(), cause.getClass().getSimpleName(), cause.getMessage());
				if (attempt >= MAX_ATTEMPTS) {
					throw new BusinessException(ErrorCode.TUTOR_UNAVAILABLE);
				}
			}
		}
	}

	private static String abbreviate(String body) {
		if (body == null) {
			return "(비어 있음)";
		}
		String oneLine = body.replaceAll("\\s+", " ");
		return oneLine.length() <= LOG_BODY_LIMIT ? oneLine : oneLine.substring(0, LOG_BODY_LIMIT) + "...";
	}

	/** candidates[0].content.parts[*].text 를 이어 붙인다. 안전 필터 등으로 비어 있으면 실패 처리 */
	private static String extractText(Map<?, ?> response) {
		List<?> candidates = (List<?>) response.get("candidates");
		Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
		StringBuilder text = new StringBuilder();
		for (Object part : (List<?>) content.get("parts")) {
			Object t = ((Map<?, ?>) part).get("text");
			if (t != null) {
				text.append(t);
			}
		}
		if (text.isEmpty()) {
			throw new NullPointerException("빈 응답");
		}
		return text.toString().trim();
	}
}
