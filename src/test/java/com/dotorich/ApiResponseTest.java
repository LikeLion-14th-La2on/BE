package com.dotorich;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.dotorich.common.exception.ErrorCode;
import com.dotorich.common.response.ApiResponse;

class ApiResponseTest {

	@Test
	void 성공_응답은_S200() {
		ApiResponse<String> res = ApiResponse.ok("ok");
		assertEquals("S200", res.code());
		assertEquals("ok", res.data());
		assertNull(res.errors());
	}

	@Test
	void 실패_응답은_에러코드와_필드오류를_담는다() {
		ApiResponse<Void> res = ApiResponse.fail(ErrorCode.INVALID_INPUT,
			List.of(new ApiResponse.FieldError("nickname", "필수 값")));
		assertEquals("E400", res.code());
		assertEquals("nickname", res.errors().get(0).field());
	}
}
