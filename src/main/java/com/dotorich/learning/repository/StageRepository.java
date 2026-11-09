package com.dotorich.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.learning.domain.Stage;

public interface StageRepository extends JpaRepository<Stage, Long> {
}
