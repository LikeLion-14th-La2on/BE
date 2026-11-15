package com.dotorich.streak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.streak.domain.UserStreak;

public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

	/** 회원탈퇴 */
	@Modifying
	@Query("delete from UserStreak s where s.userId = :userId")
	int deleteAllByUserId(Long userId);
}
