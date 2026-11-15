package com.dotorich.streak.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.streak.domain.UserStreak;

import jakarta.persistence.LockModeType;

public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

	/** 끊김 판정·단계 완료처럼 같은 사용자 요청이 동시에 와도 한 번만 처리돼야 하는 작업용 (행 잠금) */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from UserStreak s where s.userId = :userId")
	Optional<UserStreak> findByIdForUpdate(Long userId);

	/** 회원탈퇴 */
	@Modifying
	@Query("delete from UserStreak s where s.userId = :userId")
	int deleteAllByUserId(Long userId);
}
