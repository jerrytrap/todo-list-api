package com.example.todolistapi.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.exception.ServiceException;
import com.example.todolistapi.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint entrypoint;
	private static final String AUTHORIZATION_HEADER = "Authorization";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String token = jwtTokenProvider.resolveToken(request.getHeader(AUTHORIZATION_HEADER));

		if (token != null) {
			TokenStatus status = jwtTokenProvider.validateToken(token);

			switch (status) {
				case VALID -> {
					Authentication authentication = new UsernamePasswordAuthenticationToken(
						jwtTokenProvider.getUserId(token),
						"",
						List.of()
					);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
				case EXPIRED -> {
					entrypoint.commence(request, response, new CredentialsExpiredException("Access Token Expired"));
					return;
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}
