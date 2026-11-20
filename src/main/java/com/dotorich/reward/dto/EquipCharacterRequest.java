package com.dotorich.reward.dto;

import jakarta.validation.constraints.NotNull;

public record EquipCharacterRequest(@NotNull Long characterId) {
}
