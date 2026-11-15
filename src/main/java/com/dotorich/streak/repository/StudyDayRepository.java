package com.dotorich.streak.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.streak.domain.StudyDay;

public interface StudyDayRepository extends JpaRepository<StudyDay, StudyDay.Key> {
}
