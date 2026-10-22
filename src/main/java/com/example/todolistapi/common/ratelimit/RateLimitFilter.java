package com.example.todolistapi.common.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private static final String KEY_PREFIX = "rate:";
	private static final int MAX_REQUESTS = 10;
	private static final int WINDOW_SECONDS = 60;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String identifier = resolveKey(request);
		long nowSeconds = Instant.now().getEpochSecond();
		long windowIndex = nowSeconds / WINDOW_SECONDS;
		String key = KEY_PREFIX + identifier + ":" + windowIndex;

		Long count = redisTemplate.opsForValue().increment(key);
		if (count != null && count == 1L) {
			redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
		}

		if (count != null && count > MAX_REQUESTS) {
			long retryAfter = WINDOW_SECONDS - (nowSeconds % WINDOW_SECONDS);
			writeTooManyRequests(response, retryAfter);

			return;
		}

		filterChain.doFilter(request, response);
	}

	private String resolveKey(HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			return "user:" + auth.getPrincipal();
		}

		return "ip:" + request.getRemoteAddr();
	}

	private void writeTooManyRequests(HttpServletResponse response, long retryAfter) throws IOException {
		ErrorCode errorCode = ErrorCode.TOO_MANY_REQUESTS;

		response.setStatus(errorCode.getStatus().value());
		response.setHeader("Retry-After", String.valueOf(retryAfter));
		response.setContentType("application/json;charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorCode));
	}
}
