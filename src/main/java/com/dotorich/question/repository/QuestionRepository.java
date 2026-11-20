package com.dotorich.question.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionPurpose;

public interface QuestionRepository extends JpaRepository<Question, Long> {

	/** 레벨 테스트 18문제처럼 용도별 전체 (단계 → id 순) */
	List<Question> findAllByPurposeOrderByStageIdAscIdAsc(QuestionPurpose purpose);

	/** 학습 세션처럼 한 단계의 문제 (id 순) */
	List<Question> findAllByStageIdAndPurposeOrderByIdAsc(Long stageId, QuestionPurpose purpose);
}
