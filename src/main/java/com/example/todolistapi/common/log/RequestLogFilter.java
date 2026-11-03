package com.example.todolistapi.common.log;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String requestId = UUID.randomUUID().toString().substring(0, 8);
		MDC.put("requestId", requestId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			log.info(
				"HTTP method={} path={} status={}",
				request.getMethod(), request.getRequestURI(), response.getStatus()
			);
			MDC.clear();
		}
	}
}
