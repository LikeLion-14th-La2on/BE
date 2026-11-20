package com.dotorich.newsquiz.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * news_quizzes (P3). 게시일별 시사 퀴즈. 문제 내용은 questions(purpose = NEWS) 에 있다.
 * 데이터는 시드(seed_questions.json 의 publishDate)로만 넣는다. 하루에 하나.
 */
@Getter
@Entity
@Table(name = "news_quizzes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsQuiz {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "question_id", nullable = false)
	private Long questionId;

	@Column(name = "publish_date", nullable = false, unique = true)
	private LocalDate publishDate;

	/** 테스트용 */
	public NewsQuiz(Long id, Long questionId, LocalDate publishDate) {
		this.id = id;
		this.questionId = questionId;
		this.publishDate = publishDate;
	}
}
