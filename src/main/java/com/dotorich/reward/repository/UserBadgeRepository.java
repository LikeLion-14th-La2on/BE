package com.dotorich.reward.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.reward.domain.UserBadge;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadge.Key> {

	boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

	List<UserBadge> findAllByUserIdOrderByEarnedAtDesc(Long userId);

	void deleteAllByUserId(Long userId);
}
