package com.example.todolistapi.common.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
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

@Component
public class RateLimitFilter extends OncePerRequestFilter {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final int maxRequests;
	private final int windowSeconds;

	private static final String KEY_PREFIX = "rate:";

	public RateLimitFilter(
		StringRedisTemplate redisTemplate,
		ObjectMapper objectMapper,
		@Value("${rate-limit.max-requests}") int maxRequests,
		@Value("${rate-limit.window-seconds}") int windowSeconds
	) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.maxRequests = maxRequests;
		this.windowSeconds = windowSeconds;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String identifier = resolveKey(request);
		long nowSeconds = Instant.now().getEpochSecond();
		long windowIndex = nowSeconds / windowSeconds;
		String key = KEY_PREFIX + identifier + ":" + windowIndex;

		Long count = redisTemplate.opsForValue().increment(key);
		if (count != null && count == 1L) {
			redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
		}

		if (count != null && count > maxRequests) {
			long retryAfter = windowSeconds - (nowSeconds % windowSeconds);
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
