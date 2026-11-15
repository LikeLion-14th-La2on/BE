package com.dotorich.review.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.review.domain.WrongAnswer;
import com.dotorich.review.domain.WrongAnswerStatus;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {

	Optional<WrongAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);

	/** 상태별 개수 (카드뉴스 복습 대기 수 등) */
	long countByUserIdAndStatus(Long userId, WrongAnswerStatus status);

	/** 기간 안의 상태별 오답, 최신순 (오답노트 주차별 조회: start 이상 end 미만) */
	List<WrongAnswer> findAllByUserIdAndStatusAndWrongAtGreaterThanEqualAndWrongAtLessThanOrderByWrongAtDesc(
			Long userId, WrongAnswerStatus status, LocalDateTime start, LocalDateTime end);
}
