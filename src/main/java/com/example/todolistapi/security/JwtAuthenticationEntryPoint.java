package com.example.todolistapi.security;

import java.io.IOException;

import org.springframework.http.MediaType;
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
		ErrorCode errorCode = authException instanceof CredentialsExpiredException
			? ErrorCode.ACCESS_TOKEN_EXPIRED : ErrorCode.UNAUTHORIZED;

		response.setStatus(errorCode.getStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorCode));
	}
}
