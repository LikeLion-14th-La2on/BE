package com.dotorich.reward.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotorich.common.jwt.LoginUser;
import com.dotorich.common.response.ApiResponse;
import com.dotorich.reward.dto.CharacterResponse;
import com.dotorich.reward.dto.EquipCharacterRequest;
import com.dotorich.reward.dto.EquipCharacterResponse;
import com.dotorich.reward.dto.PurchaseCharacterResponse;
import com.dotorich.reward.dto.RewardResponse;
import com.dotorich.reward.service.CharacterService;
import com.dotorich.reward.service.RewardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "보상 (Part 1)")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RewardController {

	private final RewardService rewardService;
	private final CharacterService characterService;

	@Operation(summary = "보상 조회 (도토리 잔액·내역, 배지)")
	@GetMapping("/users/me/rewards")
	public ApiResponse<RewardResponse> rewards(@LoginUser Long userId) {
		return ApiResponse.ok(rewardService.getRewards(userId));
	}

	@Operation(summary = "캐릭터 목록 (가격·보유·장착 여부)")
	@GetMapping("/characters")
	public ApiResponse<List<CharacterResponse>> characters(@LoginUser Long userId) {
		return ApiResponse.ok(characterService.getCharacters(userId));
	}

	@Operation(summary = "캐릭터 구매 (도토리 차감)")
	@PostMapping("/characters/{characterId}/purchase")
	public ApiResponse<PurchaseCharacterResponse> purchase(@LoginUser Long userId, @PathVariable Long characterId) {
		return ApiResponse.ok(characterService.purchase(userId, characterId));
	}

	@Operation(summary = "캐릭터 장착")
	@PatchMapping("/users/me/character")
	public ApiResponse<EquipCharacterResponse> equip(@LoginUser Long userId,
		@Valid @RequestBody EquipCharacterRequest request) {
		return ApiResponse.ok(characterService.equip(userId, request.characterId()));
	}
}
