package com.dotorich.term.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.term.dto.BookmarkResponse;
import com.dotorich.term.dto.TermResponse;
import com.dotorich.term.dto.TermSummaryResponse;
import com.dotorich.term.service.TermService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "용어사전 (Part 3)")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TermController {

	private final TermService termService;

	@Operation(summary = "용어 검색 (이름 부분 일치, 최대 30개)")
	@GetMapping("/terms")
	public ApiResponse<List<TermSummaryResponse>> search(@RequestParam(required = false) String keyword) {
		return ApiResponse.ok(termService.search(keyword));
	}

	@Operation(summary = "용어 조회 (뜻 팝업)")
	@GetMapping("/terms/{termId}")
	public ApiResponse<TermResponse> getTerm(@LoginUser Long userId, @PathVariable Long termId) {
		return ApiResponse.ok(termService.getTermDetail(userId, termId));
	}

	@Operation(summary = "용어 북마크 추가 (이미 있으면 그대로 성공)")
	@PostMapping("/terms/{termId}/bookmark")
	public ApiResponse<Void> addBookmark(@LoginUser Long userId, @PathVariable Long termId) {
		termService.addBookmark(userId, termId);
		return ApiResponse.ok();
	}

	@Operation(summary = "용어 북마크 삭제")
	@DeleteMapping("/terms/{termId}/bookmark")
	public ApiResponse<Void> removeBookmark(@LoginUser Long userId, @PathVariable Long termId) {
		termService.removeBookmark(userId, termId);
		return ApiResponse.ok();
	}

	@Operation(summary = "북마크 목록 조회 (최신순)")
	@GetMapping("/users/me/bookmarks")
	public ApiResponse<List<BookmarkResponse>> getBookmarks(@LoginUser Long userId) {
		return ApiResponse.ok(termService.getBookmarks(userId));
	}
}
