package com.dotorich.question.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.question.domain.QuestionTerm;

public interface QuestionTermRepository extends JpaRepository<QuestionTerm, QuestionTerm.Key> {

	/** 여러 문제의 연결 용어를 한 번에 (문제마다 따로 조회하지 않도록) */
	List<QuestionTerm> findAllByQuestionIdIn(Collection<Long> questionIds);
}
