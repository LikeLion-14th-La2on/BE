package com.dotorich.home.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.home.dto.HomeResponse;
import com.dotorich.reward.service.CharacterService;
import com.dotorich.user.domain.User;
import com.dotorich.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeService {

	private final LearningStatusReader learningStatusReader;
	private final UserRepository userRepository;
	private final CharacterService characterService;

	// 끊김 판정으로 도토리가 차감될 수 있어서 쓰기 트랜잭션
	@Transactional
	public HomeResponse getHome(Long userId) {
		LearningStatusReader.Home s = learningStatusReader.read(userId);
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		return new HomeResponse(
			user.getNickname(),
			s.todaySector(),
			s.todayCompleted(),
			s.nextStage(),
			s.hasCardReview(),
			s.yesterdayWrongCount(),
			new HomeResponse.Streak(s.streakCount(), s.shieldCount(), s.weekStudied()),
			new HomeResponse.Tree(s.acornCount(), s.treeCount(), s.fallenCount(), s.acornFell()),
			user.getAcornBalance(),
			characterService.getEquippedImageUrl(user));
	}

	@Transactional(readOnly = true)
	public LearningStatusReader.Stats getStats(Long userId) {
		return learningStatusReader.stats(userId);
	}
}
