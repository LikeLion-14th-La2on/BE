package com.dotorich.term;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.term.domain.Term;
import com.dotorich.term.dto.TermResponse;
import com.dotorich.term.dto.TermSummaryResponse;
import com.dotorich.term.repository.BookmarkRepository;
import com.dotorich.term.repository.TermRepository;
import com.dotorich.term.service.TermService;

class TermServiceTest {

	private TermRepository termRepository;
	private BookmarkRepository bookmarkRepository;
	private TermService termService;
	private final Term term = new Term(10L, "원리금균등상환", "대출 원금과 이자를 합친 금액을 매달 같은 금액으로 갚는 방식",
		"매달 똑같은 돈을 내는 방식", "1억 원을 3년간 연 4%로 빌리면 매달 약 295만 원", null);

	@BeforeEach
	void setUp() {
		termRepository = mock(TermRepository.class);
		bookmarkRepository = mock(BookmarkRepository.class);
		termService = new TermService(termRepository, bookmarkRepository);
	}

	@Test
	void 용어_조회시_북마크_여부가_함께_나온다() {
		when(termRepository.findById(10L)).thenReturn(Optional.of(term));
		when(bookmarkRepository.existsByUserIdAndTermId(1L, 10L)).thenReturn(true);

		TermResponse response = termService.getTermDetail(1L, 10L);

		assertEquals("원리금균등상환", response.name());
		assertTrue(response.bookmarked());
	}

	@Test
	void 없는_용어는_TERM_NOT_FOUND() {
		when(termRepository.findById(99L)).thenReturn(Optional.empty());

		BusinessException e = assertThrows(BusinessException.class, () -> termService.getTermDetail(1L, 99L));
		assertEquals(ErrorCode.TERM_NOT_FOUND, e.getErrorCode());
		assertEquals("아직 준비되지 않은 용어입니다", e.getMessage());
	}

	@Test
	void 빈_검색어는_DB를_조회하지_않고_빈_목록() {
		assertEquals(List.of(), termService.search("   "));
		assertEquals(List.of(), termService.search(null));
		verify(termRepository, never()).search(anyString(), anyString(), any());
	}

	@Test
	void 검색어_앞뒤_공백은_지우고_검색한다() {
		when(termRepository.search(eq("원리금"), eq("원리금"), any())).thenReturn(List.of(term));

		List<TermSummaryResponse> result = termService.search(" 원리금 ");

		assertEquals(1, result.size());
		assertEquals(10L, result.get(0).termId());
	}

	@Test
	void 검색어의_퍼센트와_밑줄은_글자_그대로_찾는다() {
		termService.search("5%_!");
		verify(termRepository).search(eq("5%_!"), eq("5!%!_!!"), any());
	}

	@Test
	void 없는_용어는_북마크할_수_없다() {
		when(termRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(BusinessException.class, () -> termService.addBookmark(1L, 99L));
		verify(bookmarkRepository, never()).insertIgnore(any(), any(), any());
	}

	@Test
	void 북마크_추가는_insert_ignore로_중복을_허용한다() {
		when(termRepository.findById(10L)).thenReturn(Optional.of(term));

		termService.addBookmark(1L, 10L);

		verify(bookmarkRepository).insertIgnore(eq(1L), eq(10L), any());
	}
}
