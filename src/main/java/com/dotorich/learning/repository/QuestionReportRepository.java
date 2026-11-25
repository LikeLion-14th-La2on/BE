package com.dotorich.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.learning.domain.QuestionReport;

public interface QuestionReportRepository extends JpaRepository<QuestionReport, Long> {

	/** 회원탈퇴 */
	@Modifying
	@Query("delete from QuestionReport r where r.userId = :userId")
	int deleteAllByUserId(Long userId);
}
