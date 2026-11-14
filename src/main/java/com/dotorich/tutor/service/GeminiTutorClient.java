package com.dotorich.tutor.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.term.domain.Term;

import lombok.extern.slf4j.Slf4j;

/**
 * Gemini generateContent REST 호출.
 * https://ai.google.dev/gemini-api/docs/generate-content/text-generation
 * 최신 모델은 temperature 를 무시하고 나중에 400 을 줄 수 있다고 해서 넣지 않는다.
 */
@Slf4j
public class GeminiTutorClient implements TutorClient {

	private static final int MAX_OUTPUT_TOKENS = 800;

	private final RestClient restClient;
	private final String model;

	public GeminiTutorClient(String apiKey, String model) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(3));
		factory.setReadTimeout(Duration.ofSeconds(15));
		this.restClient = RestClient.builder()
			.baseUrl("https://generativelanguage.googleapis.com")
			.defaultHeader("x-goog-api-key", apiKey)
			.requestFactory(factory)
			.build();
		this.model = model;
	}

	@Override
	public String ask(Term term, String question) {
		Map<String, Object> body = Map.of(
			"systemInstruction", Map.of("parts", List.of(Map.of("text", TutorPrompt.system(term)))),
			"contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", question)))),
			"generationConfig", Map.of("maxOutputTokens", MAX_OUTPUT_TOKENS));
		try {
			Map<?, ?> response = restClient.post()
				.uri("/v1beta/models/{model}:generateContent", model)
				.body(body)
				.retrieve()
				.body(Map.class);
			return extractText(response);
		} catch (RestClientException | ClassCastException | NullPointerException | IndexOutOfBoundsException e) {
			log.warn("AI 튜터 호출 실패 (termId={}): {}", term.getId(), e.getMessage());
			throw new BusinessException(ErrorCode.TUTOR_UNAVAILABLE);
		}
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
