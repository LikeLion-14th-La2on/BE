package com.dotorich.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.reward.domain.Character;
import com.dotorich.reward.repository.CharacterRepository;
import com.dotorich.reward.repository.UserCharacterRepository;
import com.dotorich.reward.service.AcornService;
import com.dotorich.reward.service.CharacterService;
import com.dotorich.user.domain.User;
import com.dotorich.user.repository.UserRepository;

class CharacterServiceTest {

	@Test
	void 구매는_사용자_행을_먼저_잠그고_중복_저장은_CHARACTER_ALREADY_OWNED() {
		CharacterRepository characterRepository = mock(CharacterRepository.class);
		UserCharacterRepository userCharacterRepository = mock(UserCharacterRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		AcornService acornService = mock(AcornService.class);
		CharacterService service = new CharacterService(characterRepository, userCharacterRepository,
			userRepository, acornService);

		when(userRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(User.kakao("1")));
		when(characterRepository.findById(anyLong())).thenReturn(Optional.of(mock(Character.class)));
		when(userCharacterRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("dup"));

		BusinessException e = assertThrows(BusinessException.class, () -> service.purchase(1L, 2L));
		assertEquals(ErrorCode.CHARACTER_ALREADY_OWNED, e.getErrorCode());

		InOrder order = inOrder(userRepository, characterRepository);
		order.verify(userRepository).findByIdForUpdate(1L);
		order.verify(characterRepository).findById(2L);
	}
}
