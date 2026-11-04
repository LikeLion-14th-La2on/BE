package com.dotorich.reward.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** characters (P1). price 0 인 캐릭터는 가입 시 기본 지급된다. */
@Getter
@Entity
@Table(name = "characters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Character {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(name = "image_url", nullable = false)
	private String imageUrl;

	@Column(nullable = false)
	private int price;
}
