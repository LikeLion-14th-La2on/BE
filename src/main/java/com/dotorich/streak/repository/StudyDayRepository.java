package com.dotorich.streak.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.streak.domain.StudyDay;

public interface StudyDayRepository extends JpaRepository<StudyDay, StudyDay.Key> {

	/** 기간 안에 학습한 날 (이번 주 월~일 표시용) */
	List<StudyDay> findAllByUserIdAndStudyDateBetween(Long userId, LocalDate from, LocalDate to);

	/** 회원탈퇴 */
	@Modifying
	@Query("delete from StudyDay d where d.userId = :userId")
	int deleteAllByUserId(Long userId);
}
