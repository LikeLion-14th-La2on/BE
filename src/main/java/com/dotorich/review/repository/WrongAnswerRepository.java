package com.dotorich.review.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.review.domain.WrongAnswer;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {

	Optional<WrongAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);
}
