package com.dotorich.review.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.review.domain.WrongAnswer;
import com.dotorich.review.domain.WrongAnswerStatus;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {

	Optional<WrongAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);

	/** 상태별 개수 (카드뉴스 복습 대기 수 등) */
	long countByUserIdAndStatus(Long userId, WrongAnswerStatus status);
}
