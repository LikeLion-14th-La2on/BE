package com.dotorich.common.jwt;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/** /api/v1/** 요청의 Authorization: Bearer 토큰을 검사한다. 제외 경로는 WebConfig 참고 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

	public static final String USER_ID = "loginUserId";
	private static final String BEARER = "Bearer ";

	private final JwtProvider jwtProvider;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if ("OPTIONS".equals(request.getMethod())) {
			return true;
		}
		String header = request.getHeader("Authorization");
		Long userId = header != null && header.startsWith(BEARER)
			? jwtProvider.parseAccessToken(header.substring(BEARER.length()))
			: null;
		if (userId == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		request.setAttribute(USER_ID, userId);
		return true;
	}
}
