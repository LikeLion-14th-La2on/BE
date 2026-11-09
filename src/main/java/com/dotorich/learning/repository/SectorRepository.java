package com.dotorich.learning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.learning.domain.Sector;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    /** 전체 섹터 id (번호 순) */
    @Query("select s.id from Sector s order by s.id")
    List<Long> findAllIds();
}