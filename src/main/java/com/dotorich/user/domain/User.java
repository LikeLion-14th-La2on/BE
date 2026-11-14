package com.dotorich.user.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * users (P1). 도토리 잔액(acornBalance)은 reward.AcornService 로만 바꾼다.
 * 레벨 테스트 상태는 Part 3 이 changeLevelTestStatus 로 바꾼다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "kakao_id", length = 64, unique = true)
	private String kakaoId;

	@Column(length = 20, unique = true)
	private String nickname;

	@Column(name = "terms_agreed_at")
	private LocalDateTime termsAgreedAt;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR) // MySQL enum 칸이 아닌 varchar 로. enum 값을 추가해도 DB를 고칠 필요가 없다
	@Column(name = "level_test_status", length = 10, nullable = false)
	private LevelTestStatus levelTestStatus = LevelTestStatus.NONE;

	@Column(name = "acorn_balance", nullable = false)
	private int acornBalance;

	@Column(name = "equipped_character_id")
	private Long equippedCharacterId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static User kakao(String kakaoId) {
		User user = new User();
		user.kakaoId = kakaoId;
		return user;
	}

	public boolean isTermsAgreed() {
		return termsAgreedAt != null;
	}

	public boolean isNicknameSet() {
		return nickname != null;
	}

	public void agreeTerms() {
		if (termsAgreedAt == null) {
			termsAgreedAt = LocalDateTime.now();
		}
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	public void changeLevelTestStatus(LevelTestStatus status) {
		this.levelTestStatus = status;
	}

	public void equipCharacter(Long characterId) {
		this.equippedCharacterId = characterId;
	}

	/** AcornService 전용. 잔액이 음수가 되지 않는지는 AcornService 가 검사한다. */
	public void addAcorn(int amount) {
		this.acornBalance += amount;
	}
}
