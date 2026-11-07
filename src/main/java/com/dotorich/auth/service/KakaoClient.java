package com.dotorich.auth.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 REST API 호출.
 * kakao.client-id = REST API 키, kakao.admin-key = 연결 끊기(회원탈퇴)용 어드민 키
 */
@Slf4j
@Component
public class KakaoClient {

	private final RestClient restClient = RestClient.create();
	private final String clientId;
	private final String clientSecret;
	private final String adminKey;

	public KakaoClient(
		@Value("${kakao.client-id}") String clientId,
		@Value("${kakao.client-secret:}") String clientSecret,
		@Value("${kakao.admin-key:}") String adminKey) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.adminKey = adminKey;
	}

	/** 인가 코드 → 카카오 회원번호 */
	public String getKakaoId(String code, String redirectUri) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("grant_type", "authorization_code");
			form.add("client_id", clientId);
			form.add("redirect_uri", redirectUri);
			form.add("code", code);
			if (!clientSecret.isBlank()) {
				form.add("client_secret", clientSecret);
			}
			Map<?, ?> token = restClient.post()
				.uri("https://kauth.kakao.com/oauth/token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(Map.class);

			Map<?, ?> me = restClient.get()
				.uri("https://kapi.kakao.com/v2/user/me")
				.header("Authorization", "Bearer " + token.get("access_token"))
				.retrieve()
				.body(Map.class);
			return String.valueOf(me.get("id"));
		} catch (RestClientException | NullPointerException e) {
			log.warn("카카오 로그인 실패: {}", e.getMessage());
			throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
		}
	}

	/** 회원탈퇴 시 카카오 연결 끊기. 실패해도 탈퇴는 진행한다 (로그만 남김). */
	public void unlink(String kakaoId) {
		if (adminKey.isBlank() || kakaoId == null) {
			log.warn("카카오 연결 끊기 생략 (admin-key 없음 또는 카카오 회원 아님): {}", kakaoId);
			return;
		}
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("target_id_type", "user_id");
			form.add("target_id", kakaoId);
			restClient.post()
				.uri("https://kapi.kakao.com/v1/user/unlink")
				.header("Authorization", "KakaoAK " + adminKey)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.toBodilessEntity();
		} catch (RestClientException e) {
			log.warn("카카오 연결 끊기 실패: {}", e.getMessage());
		}
	}
}
