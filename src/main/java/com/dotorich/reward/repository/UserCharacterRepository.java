package com.dotorich.reward.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.reward.domain.UserCharacter;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, UserCharacter.Key> {

	boolean existsByUserIdAndCharacterId(Long userId, Long characterId);

	List<UserCharacter> findAllByUserId(Long userId);

	void deleteAllByUserId(Long userId);
}
