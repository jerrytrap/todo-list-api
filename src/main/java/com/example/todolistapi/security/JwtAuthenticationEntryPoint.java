package com.example.todolistapi.security;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final ObjectMapper objectMapper;

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException {
		ErrorCode errorCode;

		if (authException instanceof CredentialsExpiredException) {
			log.debug(
				"access token 만료 method={} path={}",
				request.getMethod(),
				request.getRequestURI()
			);
			errorCode = ErrorCode.ACCESS_TOKEN_EXPIRED;
		} else if (authException instanceof BadCredentialsException) {
			log.warn(
				"유효하지 않은 access token method={} path={}",
				request.getMethod(),
				request.getRequestURI()
			);
			errorCode = ErrorCode.INVALID_ACCESS_TOKEN;
		} else {
			log.debug(
				"인증되지 않음 method={} path={}",
				request.getMethod(),
				request.getRequestURI()
			);
			errorCode = ErrorCode.UNAUTHORIZED;
		}

		response.setStatus(errorCode.getStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorCode));
	}
}
