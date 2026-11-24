package com.dotorich.tutor.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.tutor.domain.TutorUsage;

import jakarta.persistence.LockModeType;

public interface TutorUsageRepository extends JpaRepository<TutorUsage, TutorUsage.Key> {

	Optional<TutorUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);

	/**
	 * 오늘 행이 없으면 0으로 만든다. 이미 있으면 그대로 둔다. MySQL 전용.
	 * insert ignore 는 중복일 때 공유 잠금만 잡아서, 같은 사용자 첫 질문이 동시에 오면 둘 다 공유 잠금을 쥔 채
	 * 다음 findForUpdate 에서 서로 배타 잠금을 기다려 교착(500)이 났다 (#18).
	 * on duplicate key update 는 처음부터 배타 잠금을 잡아 요청이 한 줄로 선다.
	 */
	@Modifying
	@Query(value = """
		insert into tutor_usage (user_id, usage_date, count) values (:userId, :usageDate, 0)
		on duplicate key update count = count
		""", nativeQuery = true)
	int createIfAbsent(Long userId, LocalDate usageDate);

	/** 횟수를 올리기 전에 행 잠금 (같은 사용자 질문이 동시에 와도 무료 횟수를 한 번씩만 쓰게) */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from TutorUsage u where u.userId = :userId and u.usageDate = :usageDate")
	Optional<TutorUsage> findForUpdate(Long userId, LocalDate usageDate);

	@Modifying
	@Query("delete from TutorUsage u where u.userId = :userId")
	int deleteAllByUserId(Long userId);
}
