package com.dotorich.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * stages (P2). 섹터마다 1~3단계.
 * id 는 시드에서 고정한다: 주식 1~3, 생활 금융 4~6, 금융 기초 7~9.
 * Part 3 문제 데이터의 stage_id 가 이 번호를 쓰므로 함부로 바꾸지 않는다.
 */
@Getter
@Entity
@Table(name = "stages",
        uniqueConstraints = @UniqueConstraint(name = "uk_stage", columnNames = {"sector_id", "step_no"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sector_id", nullable = false)
    private Long sectorId;

    /** 단계 번호 1~3 */
    @Column(name = "step_no", nullable = false)
    private int stepNo;

    @Column(nullable = false, length = 50)
    private String title;

    /** 테스트용 */
    public Stage(Long id, Long sectorId, int stepNo, String title) {
        this.id = id;
        this.sectorId = sectorId;
        this.stepNo = stepNo;
        this.title = title;
    }
}