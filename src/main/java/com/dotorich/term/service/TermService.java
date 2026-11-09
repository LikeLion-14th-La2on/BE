package com.dotorich.term.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.term.domain.Term;
import com.dotorich.term.dto.BookmarkResponse;
import com.dotorich.term.dto.TermResponse;
import com.dotorich.term.dto.TermSummaryResponse;
import com.dotorich.term.repository.BookmarkRepository;
import com.dotorich.term.repository.TermRepository;
import com.dotorich.user.domain.UserDeletedEvent;

import lombok.RequiredArgsConstructor;

/**
 * 용어사전 (P3). 용어 조회·검색, 북마크.
 * getTerm 은 AI 튜터 등 다른 Part 3 기능에서도 용어 존재 확인용으로 쓴다.
 */
@Service
@RequiredArgsConstructor
public class TermService {

	public static final int SEARCH_LIMIT = 30;
	private static final int KEYWORD_MAX_LENGTH = 50;

	private final TermRepository termRepository;
	private final BookmarkRepository bookmarkRepository;

	@Transactional(readOnly = true)
	public TermResponse getTermDetail(Long userId, Long termId) {
		Term term = getTerm(termId);
		return TermResponse.of(term, bookmarkRepository.existsByUserIdAndTermId(userId, termId));
	}

	/** 이름 부분 일치. 빈 검색어나 용어 이름보다 긴 검색어는 빈 목록 */
	@Transactional(readOnly = true)
	public List<TermSummaryResponse> search(String keyword) {
		String trimmed = keyword == null ? "" : keyword.trim();
		if (trimmed.isEmpty() || trimmed.length() > KEYWORD_MAX_LENGTH) {
			return List.of();
		}
		return termRepository.search(trimmed, escapeLike(trimmed), PageRequest.of(0, SEARCH_LIMIT)).stream()
			.map(TermSummaryResponse::of)
			.toList();
	}

	/** 이미 북마크했으면 그대로 성공 */
	@Transactional
	public void addBookmark(Long userId, Long termId) {
		getTerm(termId);
		bookmarkRepository.insertIgnore(userId, termId, LocalDateTime.now());
	}

	/** 북마크가 없어도 그대로 성공 */
	@Transactional
	public void removeBookmark(Long userId, Long termId) {
		bookmarkRepository.deleteByUserIdAndTermId(userId, termId);
	}

	@Transactional(readOnly = true)
	public List<BookmarkResponse> getBookmarks(Long userId) {
		return bookmarkRepository.findBookmarkedTerms(userId).stream()
			.map(BookmarkResponse::of)
			.toList();
	}

	@Transactional(readOnly = true)
	public Term getTerm(Long termId) {
		return termRepository.findById(termId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TERM_NOT_FOUND));
	}

	@EventListener
	public void onUserDeleted(UserDeletedEvent event) {
		bookmarkRepository.deleteAllByUserId(event.userId());
	}

	/** LIKE 의 %, _ 를 글자 그대로 찾도록 이스케이프 (escape 문자 = !) */
	static String escapeLike(String keyword) {
		return keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_");
	}
}
