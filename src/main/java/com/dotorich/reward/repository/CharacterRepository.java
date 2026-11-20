package com.dotorich.reward.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotorich.reward.domain.Character;

public interface CharacterRepository extends JpaRepository<Character, Long> {

	List<Character> findAllByPriceOrderByIdAsc(int price);

	List<Character> findAllByOrderByPriceAscIdAsc();
}
