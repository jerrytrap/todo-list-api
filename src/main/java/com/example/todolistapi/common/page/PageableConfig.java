package com.example.todolistapi.common.page;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PageableConfig implements WebMvcConfigurer {
	@Override
	public void addArgumentResolvers(
		List<HandlerMethodArgumentResolver> resolvers
	) {
		PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

		resolver.setPageParameterName("page");
		resolver.setSizeParameterName("limit");
		resolver.setOneIndexedParameters(true);

		resolvers.add(resolver);
	}
}
