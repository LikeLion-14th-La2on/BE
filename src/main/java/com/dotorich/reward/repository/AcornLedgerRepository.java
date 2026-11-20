package com.dotorich.reward.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.reward.domain.AcornLedger;
import com.dotorich.reward.domain.AcornReason;

public interface AcornLedgerRepository extends JpaRepository<AcornLedger, Long> {

	boolean existsByUserIdAndReasonAndTargetId(Long userId, AcornReason reason, Long targetId);

	List<AcornLedger> findAllByUserIdOrderByCreatedAtDescIdDesc(Long userId);

	void deleteAllByUserId(Long userId);
}
