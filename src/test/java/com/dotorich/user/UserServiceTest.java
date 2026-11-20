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
	private User user;

	@BeforeEach
	void setUp() {
		UserRepository userRepository = mock(UserRepository.class);
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
}
