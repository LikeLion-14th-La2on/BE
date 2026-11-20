package com.dotorich.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 닉네임 규칙(임시): 한글·영문·숫자 2~10자. 기획 확정되면 여기만 고치면 된다. */
public record NicknameRequest(
	@NotBlank
	@Pattern(regexp = NicknameRequest.RULE, message = "한글, 영문, 숫자 2~10자로 입력해 주세요")
	String nickname
) {
	public static final String RULE = "^[가-힣a-zA-Z0-9]{2,10}$";
}
