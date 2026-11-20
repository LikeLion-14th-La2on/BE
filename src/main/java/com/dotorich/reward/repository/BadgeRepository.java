package com.dotorich.reward.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.reward.domain.Badge;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
