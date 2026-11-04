package com.dotorich.reward.dto;

public record CharacterResponse(
	Long characterId,
	String name,
	String imageUrl,
	int price,
	boolean owned,
	boolean equipped
) {
}
