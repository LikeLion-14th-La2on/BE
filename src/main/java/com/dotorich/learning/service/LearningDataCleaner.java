package com.dotorich.learning.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.dotorich.learning.repository.QuestionReportRepository;
import com.dotorich.learning.repository.UserStageProgressRepository;
import com.dotorich.review.repository.WrongAnswerRepository;
import com.dotorich.streak.repository.StudyDayRepository;
import com.dotorich.streak.repository.TreeRepository;
import com.dotorich.streak.repository.UserStreakRepository;
import com.dotorich.user.domain.UserDeletedEvent;

import lombok.RequiredArgsConstructor;

/**
 * 회원탈퇴 시 Part 2 테이블에서 그 사용자의 줄을 지운다 (P2).
 * users 삭제 직전에 같은 트랜잭션 안에서 불린다.
 */
@Component
@RequiredArgsConstructor
public class LearningDataCleaner {

	private final UserStageProgressRepository progressRepository;
	private final UserStreakRepository userStreakRepository;
	private final StudyDayRepository studyDayRepository;
	private final TreeRepository treeRepository;
	private final WrongAnswerRepository wrongAnswerRepository;
	private final QuestionReportRepository reportRepository;

	@EventListener
	public void onUserDeleted(UserDeletedEvent event) {
		Long userId = event.userId();
		progressRepository.deleteAllByUserId(userId);
		userStreakRepository.deleteAllByUserId(userId);
		studyDayRepository.deleteAllByUserId(userId);
		treeRepository.deleteAllByUserId(userId);
		wrongAnswerRepository.deleteAllByUserId(userId);
		reportRepository.deleteAllByUserId(userId);
	}
}
