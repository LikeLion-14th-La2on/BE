package com.dotorich.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** http://localhost:8080/swagger-ui.html */
@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		String scheme = "bearerAuth";
		return new OpenAPI()
			.info(new Info().title("도토리치 API").version("v1"))
			.addSecurityItem(new SecurityRequirement().addList(scheme))
			.components(new Components().addSecuritySchemes(scheme,
				new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
	}
}
