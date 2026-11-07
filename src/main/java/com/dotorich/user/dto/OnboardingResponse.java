package com.dotorich.user.dto;

import com.dotorich.user.domain.LevelTestStatus;
import com.dotorich.user.domain.User;

public record OnboardingResponse(boolean termsAgreed, boolean nicknameSet, LevelTestStatus levelTestStatus) {

	public static OnboardingResponse of(User user) {
		return new OnboardingResponse(user.isTermsAgreed(), user.isNicknameSet(), user.getLevelTestStatus());
	}
}
