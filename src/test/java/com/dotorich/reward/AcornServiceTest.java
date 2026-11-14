package com.dotorich.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.reward.domain.AcornReason;
import com.dotorich.reward.repository.AcornLedgerRepository;
import com.dotorich.reward.service.AcornService;
import com.dotorich.user.domain.User;
import com.dotorich.user.repository.UserRepository;

class AcornServiceTest {

	private UserRepository userRepository;
	private AcornLedgerRepository ledgerRepository;
	private AcornService acornService;
	private User user;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		ledgerRepository = mock(AcornLedgerRepository.class);
		acornService = new AcornService(userRepository, ledgerRepository);
		user = User.kakao("1");
		when(userRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(user));
	}

	@Test
	void 적립하면_잔액이_늘고_내역이_남는다() {
		assertTrue(acornService.earn(1L, 1, AcornReason.DAILY_STUDY, 20261113L));
		assertEquals(1, user.getAcornBalance());
		verify(ledgerRepository).save(any());
	}

	@Test
	void 같은_날_학습_보상은_두번_주지_않는다() {
		when(ledgerRepository.existsByUserIdAndReasonAndTargetId(any(), eq(AcornReason.DAILY_STUDY), eq(20261113L)))
			.thenReturn(true);
		assertFalse(acornService.earn(1L, 1, AcornReason.DAILY_STUDY, 20261113L));
		assertEquals(0, user.getAcornBalance());
		verify(ledgerRepository, never()).save(any());
	}

	@Test
	void 잔액보다_많이_쓰면_ACORN_NOT_ENOUGH() {
		user.addAcorn(20);
		BusinessException e = assertThrows(BusinessException.class,
			() -> acornService.spend(1L, 50, AcornReason.CHARACTER, 2L));
		assertEquals(ErrorCode.ACORN_NOT_ENOUGH, e.getErrorCode());
		assertEquals(20, user.getAcornBalance());
	}

	@Test
	void 연속학습_끊기면_1개_차감() {
		user.addAcorn(3);
		assertTrue(acornService.penalizeStreakBreak(1L, 20261104L));
		assertEquals(2, user.getAcornBalance());
	}

	@Test
	void 잔액_0이면_끊겨도_차감하지_않는다() {
		assertFalse(acornService.penalizeStreakBreak(1L, 20261104L));
		assertEquals(0, user.getAcornBalance());
		verify(ledgerRepository, never()).save(any());
	}

	@Test
	void 같은_날짜_끊김은_한번만_차감() {
		user.addAcorn(3);
		when(ledgerRepository.existsByUserIdAndReasonAndTargetId(any(), eq(AcornReason.STREAK_BREAK), eq(20261104L)))
			.thenReturn(true);
		assertFalse(acornService.penalizeStreakBreak(1L, 20261104L));
		assertEquals(3, user.getAcornBalance());
	}
}
