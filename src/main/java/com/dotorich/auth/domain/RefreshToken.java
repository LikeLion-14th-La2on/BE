package com.dotorich.auth.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** refresh_tokens (P1). 재발급할 때마다 새 토큰으로 바꾼다 (기존 토큰은 삭제). */
@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false, length = 512, unique = true)
	private String token;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	public RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
		this.userId = userId;
		this.token = token;
		this.expiresAt = expiresAt;
	}

	public boolean isExpired() {
		return expiresAt.isBefore(LocalDateTime.now());
	}
}
