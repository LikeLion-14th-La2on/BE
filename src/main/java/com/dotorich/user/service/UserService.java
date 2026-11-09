package com.dotorich.user.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.auth.service.KakaoClient;
import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.reward.service.CharacterService;
import com.dotorich.user.domain.LevelTestStatus;
import com.dotorich.user.domain.User;
import com.dotorich.user.domain.UserDeletedEvent;
import com.dotorich.user.dto.MeResponse;
import com.dotorich.user.dto.NicknameCheckResponse;
import com.dotorich.user.dto.NicknameRequest;
import com.dotorich.user.dto.NicknameResponse;
import com.dotorich.user.dto.OnboardingResponse;
import com.dotorich.user.dto.TermsRequest;
import com.dotorich.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final CharacterService characterService;
	private final KakaoClient kakaoClient;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public MeResponse getMe(Long userId) {
		User user = getUser(userId);
		return new MeResponse(user.getId(), user.getNickname(), characterService.getEquippedImageUrl(user),
			user.getAcornBalance(), OnboardingResponse.of(user));
	}

	@Transactional
	public void agreeTerms(Long userId, TermsRequest request) {
		if (!request.allRequiredAgreed()) {
			throw new BusinessException(ErrorCode.TERMS_REQUIRED);
		}
		getUser(userId).agreeTerms();
	}

	@Transactional(readOnly = true)
	public NicknameCheckResponse checkNickname(String nickname) {
		boolean valid = nickname != null && nickname.matches(NicknameRequest.RULE);
		return new NicknameCheckResponse(valid && !userRepository.existsByNickname(nickname));
	}

	/** 온보딩 닉네임 등록. 이미 등록했으면 409 (수정은 2순위 닉네임 수정 API) */
	@Transactional
	public NicknameResponse registerNickname(Long userId, String nickname) {
		User user = getUser(userId);
		if (user.isNicknameSet()) {
			throw new BusinessException(ErrorCode.NICKNAME_ALREADY_SET);
		}
		if (userRepository.existsByNickname(nickname)) {
			throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
		}
		user.changeNickname(nickname);
		try {
			userRepository.flush(); // 동시에 같은 닉네임이 들어오면 유니크 제약으로 막힌다
		} catch (DataIntegrityViolationException e) {
			throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
		}
		return new NicknameResponse(nickname);
	}

	/**
	 * 레벨 테스트 결과 제출·건너뛰기 (Part 3 이 호출). status 는 DONE 또는 SKIPPED.
	 * 이미 했으면 409. 행 잠금이라 동시에 두 번 들어와도 하나만 통과한다.
	 * 호출하는 쪽 트랜잭션에 묶이므로, 뒤에서 실패하면 상태 변경도 롤백된다.
	 */
	@Transactional
	public void completeLevelTest(Long userId, LevelTestStatus status) {
		if (status == null || status == LevelTestStatus.NONE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		User user = userRepository.findByIdForUpdate(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		if (user.getLevelTestStatus() != LevelTestStatus.NONE) {
			throw new BusinessException(ErrorCode.LEVEL_TEST_ALREADY_DONE);
		}
		user.changeLevelTestStatus(status);
	}

	/** 회원탈퇴: 카카오 연결 끊기 → 각 파트 데이터 삭제(이벤트) → users 삭제 */
	@Transactional
	public void withdraw(Long userId) {
		User user = getUser(userId);
		kakaoClient.unlink(user.getKakaoId());
		eventPublisher.publishEvent(new UserDeletedEvent(userId));
		userRepository.delete(user);
	}

	private User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
