package com.dotorich.common.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dotorich.common.jwt.AuthInterceptor;
import com.dotorich.common.jwt.LoginUserArgumentResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final AuthInterceptor authInterceptor;
	private final LoginUserArgumentResolver loginUserArgumentResolver;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 로그인 없이 부를 수 있는 API 는 여기 excludePathPatterns 에 추가
		registry.addInterceptor(authInterceptor)
			.addPathPatterns("/api/v1/**")
			.excludePathPatterns(
				"/api/v1/health",
				"/api/v1/auth/kakao",
				"/api/v1/auth/refresh"
			);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(loginUserArgumentResolver);
	}
}
