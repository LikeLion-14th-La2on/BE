package com.dotorich.streak.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.reward.service.AcornService;
import com.dotorich.streak.domain.Tree;
import com.dotorich.streak.domain.UserStreak;
import com.dotorich.streak.repository.TreeRepository;
import com.dotorich.streak.repository.UserStreakRepository;

import lombok.RequiredArgsConstructor;

/**
 * 접속 시 끊김 판정 (P2). 홈 조회에서 부른다 (별도 API 없음, 결과는 홈 응답의 acornFell).
 * 1. 마지막 학습일이 그저께 이전이면 끊김. 끊김 날짜 = 마지막 학습일 + 1일
 * 2. 이미 처리한 끊김이면(last_break_date 와 같으면) 아무것도 안 함 → 며칠을 쉬어도 끊김 1회당 1번
 * 3. 보호권이 있으면 1개 써서 연속 기록 유지 (마지막 학습일을 어제로 맞춤)
 * 4. 없으면 스트릭 0, 도토리 1개 차감(잔액 0이면 안 함), 나무에서 1개 떨어뜨림(0칸이면 안 함), 끊김 날짜 기록
 */
@Service
@RequiredArgsConstructor
public class StreakBreakService {

	private final UserStreakRepository userStreakRepository;
	private final TreeRepository treeRepository;
	private final AcornService acornService;

	/** 끊김을 처리해서 나무 도토리가 떨어졌으면 true (떨어지는 애니메이션용) */
	@Transactional
	public boolean check(Long userId, LocalDate today) {
		UserStreak streak = userStreakRepository.findById(userId).orElse(null);
		if (streak == null) {
			return false; // 한 번도 학습하지 않은 사용자
		}

		LocalDate breakDate = streak.unhandledBreakDate(today);
		if (breakDate == null) {
			return false;
		}

		if (streak.hasShield()) {
			streak.useShield(today);
			return false;
		}

		streak.breakStreak(breakDate);
		acornService.penalizeStreakBreak(userId, toYyyymmdd(breakDate));
		return treeRepository.findFirstByUserIdAndCompletedAtIsNull(userId)
				.map(Tree::dropAcorn)
				.orElse(false);
	}

	private static long toYyyymmdd(LocalDate date) {
		return Long.parseLong(date.format(DateTimeFormatter.BASIC_ISO_DATE));
	}
}
