package com.dotorich.term.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.term.domain.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Bookmark.Key> {

	boolean existsByUserIdAndTermId(Long userId, Long termId);

	/**
	 * 이미 있으면 무시 (버튼을 두 번 눌러도 PK 충돌 없이 성공). MySQL 전용.
	 * created_at 은 DB 시간대와 상관없이 KST 로 넣으려고 서버에서 넘긴다.
	 */
	@Modifying
	@Query(value = "insert ignore into bookmarks (user_id, term_id, created_at) values (:userId, :termId, :now)",
		nativeQuery = true)
	int insertIgnore(Long userId, Long termId, LocalDateTime now);

	/** 내 북마크 최신순 (용어 이름·쉬운 설명과 함께) */
	@Query("""
		select b.termId as termId, t.name as name, t.easyExplanation as easyExplanation, b.createdAt as createdAt
		from Bookmark b join Term t on t.id = b.termId
		where b.userId = :userId
		order by b.createdAt desc, b.termId desc
		""")
	List<BookmarkedTerm> findBookmarkedTerms(Long userId);

	@Modifying
	@Query("delete from Bookmark b where b.userId = :userId and b.termId = :termId")
	int deleteByUserIdAndTermId(Long userId, Long termId);

	@Modifying
	@Query("delete from Bookmark b where b.userId = :userId")
	int deleteAllByUserId(Long userId);

	interface BookmarkedTerm {
		Long getTermId();

		String getName();

		String getEasyExplanation();

		LocalDateTime getCreatedAt();
	}
}
