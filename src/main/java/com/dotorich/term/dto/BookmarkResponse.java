package com.dotorich.term.dto;

import java.time.OffsetDateTime;

import com.dotorich.common.util.KstTime;
import com.dotorich.term.repository.BookmarkRepository.BookmarkedTerm;

/** 북마크 목록 한 줄. createdAt = 북마크한 시각 */
public record BookmarkResponse(
	Long termId,
	String name,
	String easyExplanation,
	OffsetDateTime createdAt
) {

	public static BookmarkResponse of(BookmarkedTerm row) {
		return new BookmarkResponse(row.getTermId(), row.getName(), row.getEasyExplanation(),
			KstTime.of(row.getCreatedAt()));
	}
}
