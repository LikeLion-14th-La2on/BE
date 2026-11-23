package com.dotorich.newsquiz.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.newsquiz.domain.NewsQuiz;

public interface NewsQuizRepository extends JpaRepository<NewsQuiz, Long> {

	/** 오늘(없으면 가장 최근) 게시된 퀴즈. 미래 날짜는 제외 */
	Optional<NewsQuiz> findFirstByPublishDateLessThanEqualOrderByPublishDateDesc(LocalDate date);
}
