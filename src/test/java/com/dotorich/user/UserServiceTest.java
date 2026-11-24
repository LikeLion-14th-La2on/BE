package com.dotorich.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.user.domain.LevelTestStatus;
import com.dotorich.user.domain.User;
import com.dotorich.user.repository.UserRepository;
import com.dotorich.user.service.UserService;

class UserServiceTest {

	private UserService userService;
	private UserRepository userRepository;
	private User user;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		userService = new UserService(userRepository, null, null, null);
		user = User.kakao("1");
		when(userRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(user));
	}

	@Test
	void 레벨테스트_완료하면_상태가_바뀐다() {
		userService.completeLevelTest(1L, LevelTestStatus.SKIPPED);
		assertEquals(LevelTestStatus.SKIPPED, user.getLevelTestStatus());
	}

	@Test
	void 이미_했으면_LEVEL_TEST_ALREADY_DONE() {
		userService.completeLevelTest(1L, LevelTestStatus.DONE);
		BusinessException e = assertThrows(BusinessException.class,
			() -> userService.completeLevelTest(1L, LevelTestStatus.SKIPPED));
		assertEquals(ErrorCode.LEVEL_TEST_ALREADY_DONE, e.getErrorCode());
		assertEquals(LevelTestStatus.DONE, user.getLevelTestStatus());
	}

	@Test
	void NONE으로_부르면_INVALID_INPUT() {
		BusinessException e = assertThrows(BusinessException.class,
			() -> userService.completeLevelTest(1L, LevelTestStatus.NONE));
		assertEquals(ErrorCode.INVALID_INPUT, e.getErrorCode());
	}

	@Test
	void 닉네임_수정() {
		user.changeNickname("도토리");
		assertEquals("다람쥐", userService.updateNickname(1L, "다람쥐").nickname());
		assertEquals("다람쥐", user.getNickname());
	}

	@Test
	void 지금과_같은_닉네임이면_중복_검사_없이_성공() {
		user.changeNickname("도토리");
		when(userRepository.existsByNickname("도토리")).thenReturn(true);
		assertEquals("도토리", userService.updateNickname(1L, "도토리").nickname());
	}

	@Test
	void 다른_사람이_쓰는_닉네임으로_수정하면_NICKNAME_DUPLICATED() {
		user.changeNickname("도토리");
		when(userRepository.existsByNickname("다람쥐")).thenReturn(true);
		BusinessException e = assertThrows(BusinessException.class, () -> userService.updateNickname(1L, "다람쥐"));
		assertEquals(ErrorCode.NICKNAME_DUPLICATED, e.getErrorCode());
		assertEquals("도토리", user.getNickname());
	}
}
