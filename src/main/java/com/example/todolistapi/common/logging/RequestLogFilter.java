package com.example.todolistapi.common.logging;

import static com.example.todolistapi.common.logging.MdcKey.*;

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
		MDC.put(REQUEST_ID, requestId);
		long startTime = System.nanoTime();

		try {
			filterChain.doFilter(request, response);
		} finally {
			Object memberId = request.getAttribute(MEMBER_ID);
			if (memberId != null) {
				MDC.put(MEMBER_ID, memberId.toString());
			}

			long duration = (System.nanoTime() - startTime) / 1_000_000;

			log.info(
				"HTTP method={} path={} status={}, duration={}ms",
				request.getMethod(), request.getRequestURI(), response.getStatus(), duration
			);

			MDC.clear();
		}
	}
}
