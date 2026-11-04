package com.dotorich.common.jwt;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.dotorich.common.exception.BusinessException;
import com.dotorich.common.exception.ErrorCode;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUser.class) && Long.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		Object userId = webRequest.getAttribute(AuthInterceptor.USER_ID, RequestAttributes.SCOPE_REQUEST);
		if (userId == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		return userId;
	}
}
