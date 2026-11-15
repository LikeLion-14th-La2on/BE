package com.dotorich.streak.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.streak.domain.Tree;

public interface TreeRepository extends JpaRepository<Tree, Long> {

	/** 키우는 중인 나무 (completed_at 이 비어 있는 것) */
	Optional<Tree> findFirstByUserIdAndCompletedAtIsNull(Long userId);
}
