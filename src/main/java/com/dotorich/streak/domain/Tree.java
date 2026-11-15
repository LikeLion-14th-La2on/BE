package com.dotorich.streak.domain;

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

/**
 * trees (P2). 도토리나무와 내 숲.
 * completed_at 이 비어 있으면 키우는 중인 나무(사용자당 1그루), 값이 있으면 내 숲의 완성 나무다.
 * 나무 번호(id)는 도토리 TREE_COMPLETE 의 targetId 로 쓴다.
 */
@Getter
@Entity
@Table(name = "trees")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tree {

	/** 나무 한 그루의 칸 수 (회의 결정: 7칸) */
	public static final int SIZE = 7;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	/** 열린 도토리 0~7 */
	@Column(name = "filled_count", nullable = false)
	private int filledCount;

	/** 끊김으로 떨어진 도토리 (나무가 완성되면 그대로 내 숲으로 간다) */
	@Column(name = "fallen_count", nullable = false)
	private int fallenCount;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	/** 새 나무 */
	public static Tree start(Long userId) {
		Tree tree = new Tree();
		tree.userId = userId;
		return tree;
	}

	/** 하루 학습으로 1칸 채운다. 7칸이 되면 완성 처리하고 true */
	public boolean grow(LocalDateTime now) {
		filledCount++;
		if (filledCount >= SIZE) {
			completedAt = now;
			return true;
		}
		return false;
	}

	/** 끊김: 열린 도토리가 있으면 1개 떨어뜨리고 true (0개면 아무 일 없음) */
	public boolean dropAcorn() {
		if (filledCount == 0) {
			return false;
		}
		filledCount--;
		fallenCount++;
		return true;
	}
}
