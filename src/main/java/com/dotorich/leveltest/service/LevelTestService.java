package com.dotorich.leveltest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.learning.service.StageReader.StageInfo;
import com.dotorich.leveltest.dto.LevelTestQuestionsResponse;
import com.dotorich.leveltest.dto.LevelTestResultResponse;
import com.dotorich.leveltest.dto.LevelTestSubmitRequest;
import com.dotorich.leveltest.service.LevelTestStageGateway.SectorInfo;
import com.dotorich.question.domain.Question;
import com.dotorich.question.domain.QuestionPurpose;
import com.dotorich.question.repository.QuestionRepository;
import com.dotorich.question.service.AnswerGrader;
import com.dotorich.user.domain.LevelTestStatus;
import com.dotorich.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 레벨 테스트 (P3). 섹터마다 단계당 2문제 × 3단계 = 6문제.
 *
 * 회의 결정 (11/14)
 * - 단계 통과: 그 단계 2문제 모두 정답 ('잘 모르겠어요'는 오답)
 * - 시작 단계: 1단계부터 순서대로 보고 처음으로 통과 못 한 단계. 전부 통과하면 3단계
 * - 레벨 테스트로 넘긴 단계는 도토리 보상 없음 (여기서는 도토리를 건드리지 않는다)
 *
 * 이미 레벨 테스트를 했거나 건너뛰었으면 UserService.completeLevelTest 가 409 를 던진다.
 * 같은 트랜잭션이라 뒤에서 실패하면 상태 변경도 롤백된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LevelTestService {

	public static final int QUESTIONS_PER_STEP = 2;
	public static final int MAX_STEP = 3;

	private final QuestionRepository questionRepository;
	private final LevelTestStageGateway stageGateway;
	private final UserService userService;

	@Transactional(readOnly = true)
	public LevelTestQuestionsResponse getQuestions() {
		Map<Long, TreeMap<Integer, List<Question>>> bySector = loadQuestions();
		List<LevelTestQuestionsResponse.SectorQuestions> sectors = stageGateway.getAllSectors().stream()
			.map(sector -> {
				List<LevelTestQuestionsResponse.QuestionItem> items = new ArrayList<>();
				bySector.getOrDefault(sector.sectorId(), new TreeMap<>()).forEach((stepNo, questions) ->
					questions.forEach(q -> items.add(new LevelTestQuestionsResponse.QuestionItem(
						q.getId(), stepNo, q.getType().name(), q.getContent(), q.getChoices()))));
				return new LevelTestQuestionsResponse.SectorQuestions(sector.sectorId(), sector.name(), items);
			})
			.toList();
		return new LevelTestQuestionsResponse(sectors);
	}

	@Transactional
	public LevelTestResultResponse submit(Long userId, LevelTestSubmitRequest request) {
		userService.completeLevelTest(userId, LevelTestStatus.DONE);

		Map<Long, TreeMap<Integer, List<Question>>> bySector = loadQuestions();
		Map<Long, String> answers = toAnswerMap(request, bySector);

		Map<Long, Integer> startStepBySector = new LinkedHashMap<>();
		List<LevelTestResultResponse.SectorResult> results = new ArrayList<>();
		List<LevelTestResultResponse.Explanation> explanations = new ArrayList<>();

		for (SectorInfo sector : stageGateway.getAllSectors()) {
			TreeMap<Integer, List<Question>> bySteps = bySector.getOrDefault(sector.sectorId(), new TreeMap<>());
			List<Boolean> stepPassed = new ArrayList<>();
			for (int stepNo = 1; stepNo <= MAX_STEP; stepNo++) {
				List<Question> questions = bySteps.getOrDefault(stepNo, List.of());
				boolean passed = !questions.isEmpty();
				for (Question q : questions) {
					boolean correct = AnswerGrader.isCorrect(q, answers.get(q.getId()));
					passed &= correct;
					explanations.add(new LevelTestResultResponse.Explanation(
						q.getId(), correct, AnswerGrader.displayAnswer(q), q.getExplanation()));
				}
				stepPassed.add(passed);
			}
			int startStep = startStep(stepPassed);
			startStepBySector.put(sector.sectorId(), startStep);
			results.add(new LevelTestResultResponse.SectorResult(sector.sectorId(), sector.name(), startStep));
		}

		stageGateway.initStartStages(userId, startStepBySector);
		return new LevelTestResultResponse(results, explanations);
	}

	/** 건너뛰기: 모든 섹터 1단계부터 */
	@Transactional
	public void skip(Long userId) {
		userService.completeLevelTest(userId, LevelTestStatus.SKIPPED);
		Map<Long, Integer> startStepBySector = new LinkedHashMap<>();
		stageGateway.getAllSectors().forEach(sector -> startStepBySector.put(sector.sectorId(), 1));
		stageGateway.initStartStages(userId, startStepBySector);
	}

	/** 1단계부터 순서대로 보고 처음으로 통과 못 한 단계. 전부 통과하면 마지막 단계(3) */
	static int startStep(List<Boolean> stepPassed) {
		for (int i = 0; i < stepPassed.size(); i++) {
			if (!stepPassed.get(i)) {
				return i + 1;
			}
		}
		return MAX_STEP;
	}

	/** 레벨 테스트 문제를 섹터 → 단계 → 문제(id 순, 단계당 2개) 로 묶는다 */
	private Map<Long, TreeMap<Integer, List<Question>>> loadQuestions() {
		Map<Long, StageInfo> stageCache = new HashMap<>();
		Map<Long, TreeMap<Integer, List<Question>>> bySector = new HashMap<>();
		for (Question q : questionRepository.findAllByPurposeOrderByStageIdAscIdAsc(QuestionPurpose.LEVEL_TEST)) {
			if (q.getStageId() == null) {
				log.warn("레벨 테스트 문제에 stage_id 가 없음: questionId={}", q.getId());
				continue;
			}
			StageInfo stage = stageCache.computeIfAbsent(q.getStageId(), stageGateway::getStageInfo);
			List<Question> step = bySector.computeIfAbsent(stage.sectorId(), id -> new TreeMap<>())
				.computeIfAbsent(stage.stepNo(), n -> new ArrayList<>());
			if (step.size() < QUESTIONS_PER_STEP) {
				step.add(q);
			}
		}
		return bySector;
	}

	/** 레벨 테스트에 없는 문제 id 가 오면 400. 답을 안 보낸 문제는 오답 처리 */
	private static Map<Long, String> toAnswerMap(LevelTestSubmitRequest request,
		Map<Long, TreeMap<Integer, List<Question>>> bySector) {
		Map<Long, Boolean> valid = new HashMap<>();
		bySector.values().forEach(steps -> steps.values().forEach(qs -> qs.forEach(q -> valid.put(q.getId(), true))));
		Map<Long, String> answers = new HashMap<>();
		for (LevelTestSubmitRequest.Answer a : request.answers()) {
			if (!valid.containsKey(a.questionId())) {
				throw new BusinessException(ErrorCode.INVALID_INPUT);
			}
			answers.put(a.questionId(), a.answer());
		}
		return answers;
	}
}
