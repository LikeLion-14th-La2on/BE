package com.dotorich.learning.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.learning.domain.UserStageProgress;

public interface UserStageProgressRepository extends JpaRepository<UserStageProgress, UserStageProgress.Key> {

	/** 한 사용자의 여러 단계 상태를 한 번에 */
	List<UserStageProgress> findAllByUserIdAndStageIdIn(Long userId, Collection<Long> stageIds);

	/** 한 사용자의 단계 상태 전부 (마이페이지 진행도용) */
	List<UserStageProgress> findAllByUserId(Long userId);

	/** 기록이 있으면 열린 단계 (없으면 잠김) */
	boolean existsByUserIdAndStageId(Long userId, Long stageId);

	/** 단계 기록이 하나라도 있는지 (레벨 테스트 결과를 이미 저장했는지) */
	boolean existsByUserId(Long userId);

	Optional<UserStageProgress> findByUserIdAndStageId(Long userId, Long stageId);
}
