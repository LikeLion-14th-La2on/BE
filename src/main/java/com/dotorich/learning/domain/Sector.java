package com.dotorich.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * sectors (P2). 주식, 생활 금융, 금융 기초.
 * 데이터는 시드(data.sql)로만 넣고, API 로 수정하지 않는다.
 */
@Getter
@Entity
@Table(name = "sectors")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sector {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 30)
	private String name;

	/** 요일 순환 순서 (오늘의 섹터 계산용) */
	@Column(name = "rotation_order", nullable = false)
	private int rotationOrder;

	/** 테스트용 */
	public Sector(Long id, String name, int rotationOrder) {
		this.id = id;
		this.name = name;
		this.rotationOrder = rotationOrder;
	}
}
