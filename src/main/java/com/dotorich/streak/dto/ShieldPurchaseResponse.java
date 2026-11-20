package com.dotorich.streak.dto;

/** 보호권 구매 응답. shieldCount: 구매 후 보유 보호권, acornBalance: 구매 후 도토리 잔액 */
public record ShieldPurchaseResponse(
		int shieldCount,
		int acornBalance
) {
}
