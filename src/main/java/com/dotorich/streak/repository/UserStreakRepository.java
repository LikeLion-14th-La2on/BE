package com.dotorich.streak.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.streak.domain.UserStreak;

public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {
}
