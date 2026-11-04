package com.dotorich.reward.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;
import com.dotorich.reward.domain.AcornReason;
import com.dotorich.reward.domain.Character;
import com.dotorich.reward.domain.UserCharacter;
import com.dotorich.reward.dto.CharacterResponse;
import com.dotorich.reward.dto.EquipCharacterResponse;
import com.dotorich.reward.dto.PurchaseCharacterResponse;
import com.dotorich.reward.repository.CharacterRepository;
import com.dotorich.reward.repository.UserCharacterRepository;
import com.dotorich.user.domain.User;
import com.dotorich.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CharacterService {

	private final CharacterRepository characterRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final UserRepository userRepository;
	private final AcornService acornService;

	@Transactional(readOnly = true)
	public List<CharacterResponse> getCharacters(Long userId) {
		User user = getUser(userId);
		Set<Long> owned = userCharacterRepository.findAllByUserId(userId).stream()
			.map(UserCharacter::getCharacterId)
			.collect(Collectors.toSet());
		return characterRepository.findAllByOrderByPriceAscIdAsc().stream()
			.map(c -> new CharacterResponse(c.getId(), c.getName(), c.getImageUrl(), c.getPrice(),
				owned.contains(c.getId()), c.getId().equals(user.getEquippedCharacterId())))
			.toList();
	}

	@Transactional
	public PurchaseCharacterResponse purchase(Long userId, Long characterId) {
		Character character = getCharacter(characterId);
		if (userCharacterRepository.existsByUserIdAndCharacterId(userId, characterId)) {
			throw new BusinessException(ErrorCode.CHARACTER_ALREADY_OWNED);
		}
		acornService.spend(userId, character.getPrice(), AcornReason.CHARACTER, characterId);
		userCharacterRepository.save(new UserCharacter(userId, characterId));
		return new PurchaseCharacterResponse(characterId, acornService.getBalance(userId));
	}

	@Transactional
	public EquipCharacterResponse equip(Long userId, Long characterId) {
		Character character = getCharacter(characterId);
		if (!userCharacterRepository.existsByUserIdAndCharacterId(userId, characterId)) {
			throw new BusinessException(ErrorCode.CHARACTER_NOT_OWNED);
		}
		getUser(userId).equipCharacter(characterId);
		return new EquipCharacterResponse(characterId, character.getImageUrl());
	}

	/** 가입 시 price 0 캐릭터를 모두 지급하고 첫 번째를 장착한다. */
	@Transactional
	public void grantDefaultCharacters(User user) {
		List<Character> free = characterRepository.findAllByPriceOrderByIdAsc(0);
		for (Character c : free) {
			if (!userCharacterRepository.existsByUserIdAndCharacterId(user.getId(), c.getId())) {
				userCharacterRepository.save(new UserCharacter(user.getId(), c.getId()));
			}
		}
		if (user.getEquippedCharacterId() == null && !free.isEmpty()) {
			user.equipCharacter(free.get(0).getId());
		}
	}

	@Transactional(readOnly = true)
	public String getEquippedImageUrl(User user) {
		Long id = user.getEquippedCharacterId();
		return id == null ? null : characterRepository.findById(id).map(Character::getImageUrl).orElse(null);
	}

	private Character getCharacter(Long characterId) {
		return characterRepository.findById(characterId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_NOT_FOUND));
	}

	private User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
