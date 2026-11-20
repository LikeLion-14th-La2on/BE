package com.dotorich.review.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.util.KstTime;
import com.dotorich.learning.domain.Sector;
import com.dotorich.learning.domain.Stage;
import com.dotorich.learning.repository.SectorRepository;
import com.dotorich.learning.repository.StageRepository;
import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionType;
import com.dotorich.question.repository.QuestionRepository;
import com.dotorich.question.service.AnswerGrader;
import com.dotorich.review.domain.WrongAnswer;
import com.dotorich.review.domain.WrongAnswerStatus;
import com.dotorich.review.dto.WrongAnswerWeekResponse;
import com.dotorich.review.dto.WrongAnswerWeekResponse.Item;
import com.dotorich.review.repository.WrongAnswerRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * 오답노트 주차별 조회 (P2). 다시 풀지 않고 해설만 보여준다.
 * - 오답노트(IN_NOTE)만, wrong_at(오답노트에 들어간 시각, KST) 기준 월~일
 * - week 를 빼면 이번 주, sectorId 를 빼면 전체 섹터
 * - weekLabel 은 그 주 월요일이 속한 달 기준 (2026-W45 → 11월 1주차)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WrongAnswerService {

	private static final Pattern WEEK_PATTERN = Pattern.compile("^(\\d{4})-W(\\d{2})$");

	private final WrongAnswerRepository wrongAnswerRepository;
	private final QuestionRepository questionRepository;
	private final StageRepository stageRepository;
	private final SectorRepository sectorRepository;
	private final ObjectMapper objectMapper;

	public WrongAnswerWeekResponse getWeek(Long userId, String week, Long sectorId) {
		LocalDate monday = toMonday(week);
		if (sectorId != null && !sectorRepository.existsById(sectorId)) {
			throw new BusinessException(ErrorCode.SECTOR_NOT_FOUND);
		}

		List<WrongAnswer> wrongs = wrongAnswerRepository
				.findAllByUserIdAndStatusAndWrongAtGreaterThanEqualAndWrongAtLessThanOrderByWrongAtDesc(
						userId, WrongAnswerStatus.IN_NOTE, monday.atStartOfDay(), monday.plusWeeks(1).atStartOfDay());

		// 오답 → 문제 → 단계 → 섹터 를 한 번씩만 조회해서 이어 붙인다
		Map<Long, Question> questionById = questionRepository
				.findAllById(wrongs.stream().map(WrongAnswer::getQuestionId).toList()).stream()
				.collect(Collectors.toMap(Question::getId, Function.identity()));
		Map<Long, Long> sectorIdByStageId = stageRepository
				.findAllById(questionById.values().stream().map(Question::getStageId).filter(Objects::nonNull).toList())
				.stream()
				.collect(Collectors.toMap(Stage::getId, Stage::getSectorId));
		Map<Long, String> sectorNameById = sectorRepository.findAll().stream()
				.collect(Collectors.toMap(Sector::getId, Sector::getName));

		List<Item> items = wrongs.stream()
				.filter(wrong -> questionById.containsKey(wrong.getQuestionId()))
				.map(wrong -> {
					Question question = questionById.get(wrong.getQuestionId());
					Long questionSectorId = question.getStageId() == null ? null
							: sectorIdByStageId.get(question.getStageId());
					if (sectorId != null && !sectorId.equals(questionSectorId)) {
						return null;
					}
					return toItem(wrong, question, sectorNameById.get(questionSectorId));
				})
				.filter(Objects::nonNull)
				.toList();

		return new WrongAnswerWeekResponse(toWeekText(monday), toWeekLabel(monday), items);
	}

	private Item toItem(WrongAnswer wrong, Question question, String sectorName) {
		boolean matching = question.getType() == QuestionType.MATCHING;
		Object myAnswer = matching ? readMap(wrong.getMyAnswer()) : wrong.getMyAnswer();
		Object correctAnswer = matching ? readMap(question.getAnswer()) : AnswerGrader.displayAnswer(question);
		return new Item(wrong.getId(), question.getId(), sectorName, question.getType(), question.getContent(),
				question.getChoices(), myAnswer, correctAnswer, question.getExplanation(), question.getReference(),
				KstTime.of(wrong.getWrongAt()));
	}

	/** "2026-W45" → 그 주 월요일. 빼면 이번 주. 형식이 틀리거나 없는 주차면 400 */
	private static LocalDate toMonday(String week) {
		if (week == null || week.isBlank()) {
			return LocalDate.now(KstTime.ZONE).with(DayOfWeek.MONDAY);
		}
		Matcher matcher = WEEK_PATTERN.matcher(week.trim());
		if (!matcher.matches()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		int year = Integer.parseInt(matcher.group(1));
		int weekNo = Integer.parseInt(matcher.group(2));
		LocalDate jan4 = LocalDate.of(year, 1, 4); // 1월 4일은 항상 그 해 1주차
		if (!IsoFields.WEEK_OF_WEEK_BASED_YEAR.rangeRefinedBy(jan4).isValidValue(weekNo)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		return jan4.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNo).with(DayOfWeek.MONDAY);
	}

	/** 월요일 → "2026-W45" */
	private static String toWeekText(LocalDate monday) {
		return String.format("%d-W%02d", monday.get(IsoFields.WEEK_BASED_YEAR),
				monday.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
	}

	/** 월요일 → "11월 1주차" (그 달의 몇 번째 월요일인지) */
	private static String toWeekLabel(LocalDate monday) {
		return monday.getMonthValue() + "월 " + ((monday.getDayOfMonth() - 1) / 7 + 1) + "주차";
	}

	/** 매칭 답 JSON 을 {"용어": "뜻"} 객체로. 읽을 수 없으면 데이터 문제라 500 */
	private Map<?, ?> readMap(String json) {
		if (json == null) {
			return null;
		}
		try {
			return objectMapper.readValue(json, Map.class);
		} catch (JacksonException e) {
			throw new IllegalStateException("매칭 답 JSON 형식 오류", e);
		}
	}
}
