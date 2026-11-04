package com.dotorich.user.dto;

import jakarta.validation.constraints.NotNull;

public record TermsRequest(@NotNull Boolean serviceTerms, @NotNull Boolean privacyPolicy) {

	public boolean allRequiredAgreed() {
		return serviceTerms && privacyPolicy;
	}
}
