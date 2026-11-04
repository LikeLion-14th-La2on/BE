package com.dotorich.common.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Access Token 30분, Refresh Token 14일. 두 토큰은 type 클레임으로 구분한다. */
@Component
public class JwtProvider {

	public static final Duration ACCESS_TTL = Duration.ofMinutes(30);
	public static final Duration REFRESH_TTL = Duration.ofDays(14);

	private static final String TYPE = "type";
	private static final String ACCESS = "access";
	private static final String REFRESH = "refresh";

	private final SecretKey key;

	public JwtProvider(@Value("${jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(Long userId) {
		return create(userId, ACCESS, ACCESS_TTL);
	}

	public String createRefreshToken(Long userId) {
		return create(userId, REFRESH, REFRESH_TTL);
	}

	/** 유효한 Access Token 이면 userId, 아니면 null */
	public Long parseAccessToken(String token) {
		return parse(token, ACCESS);
	}

	/** 유효한 Refresh Token 이면 userId, 아니면 null */
	public Long parseRefreshToken(String token) {
		return parse(token, REFRESH);
	}

	private String create(Long userId, String type, Duration ttl) {
		Date now = new Date();
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim(TYPE, type)
			.id(java.util.UUID.randomUUID().toString())
			.issuedAt(now)
			.expiration(new Date(now.getTime() + ttl.toMillis()))
			.signWith(key)
			.compact();
	}

	private Long parse(String token, String expectedType) {
		try {
			Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
			if (!expectedType.equals(claims.get(TYPE, String.class))) {
				return null;
			}
			return Long.valueOf(claims.getSubject());
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}
}
