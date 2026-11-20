package com.dotorich.user.dto;

public record MeResponse(
	Long userId,
	String nickname,
	String characterImageUrl,
	int acornBalance,
	OnboardingResponse onboarding
) {
}
