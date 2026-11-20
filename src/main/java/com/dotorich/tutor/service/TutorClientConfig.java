package com.dotorich.tutor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/** gemini.api-key 가 있으면 Gemini, 없으면 가짜 튜터를 쓴다. 키는 백엔드 환경변수에만 둔다. */
@Slf4j
@Configuration
public class TutorClientConfig {

	@Bean
	public TutorClient tutorClient(
		@Value("${gemini.api-key:}") String apiKey,
		@Value("${gemini.model:gemini-3.5-flash-lite}") String model) {
		if (apiKey.isBlank()) {
			log.warn("gemini.api-key 가 없어 개발용 가짜 AI 튜터를 사용합니다");
			return new FakeTutorClient();
		}
		log.info("AI 튜터 모델: {}", model);
		return new GeminiTutorClient(apiKey, model);
	}
}
