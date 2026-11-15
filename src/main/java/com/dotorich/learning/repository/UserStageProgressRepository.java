package com.dotorich.learning.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.learning.domain.UserStageProgress;

public interface UserStageProgressRepository extends JpaRepository<UserStageProgress, UserStageProgress.Key> {

	/** 한 사용자의 여러 단계 상태를 한 번에 */
	List<UserStageProgress> findAllByUserIdAndStageIdIn(Long userId, Collection<Long> stageIds);
}
