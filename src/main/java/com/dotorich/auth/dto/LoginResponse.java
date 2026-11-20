package com.dotorich.auth.dto;

import com.dotorich.user.dto.OnboardingResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
	String accessToken,
	String refreshToken,
	@JsonProperty("isNewUser") boolean isNewUser,
	OnboardingResponse onboarding
) {
}
