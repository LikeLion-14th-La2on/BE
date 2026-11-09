package com.dotorich.term.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** bookmarks (P3). (user_id, term_id) 가 PK 라서 같은 용어는 한 번만 북마크된다. */
@Getter
@Entity
@Table(name = "bookmarks")
@IdClass(Bookmark.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "term_id")
	private Long termId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Bookmark(Long userId, Long termId) {
		this.userId = userId;
		this.termId = termId;
	}

	@EqualsAndHashCode
	@NoArgsConstructor
	public static class Key implements Serializable {
		private Long userId;
		private Long termId;
	}
}
