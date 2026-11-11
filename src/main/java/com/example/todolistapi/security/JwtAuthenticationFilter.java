package com.example.todolistapi.security;

import static com.example.todolistapi.common.logging.MdcKey.*;

import java.io.IOException;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		try {
			String token = jwtTokenProvider.resolveToken(request.getHeader(AUTHORIZATION_HEADER));

			if (token != null && !authenticate(token, request, response)) {
				return;
			}

			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MEMBER_ID);
		}
	}

	private boolean authenticate(
		String token,
		HttpServletRequest request,
		HttpServletResponse response
	) throws IOException {
		try {
			Long memberId = jwtTokenProvider.getMemberId(token);

			Authentication authentication = new UsernamePasswordAuthenticationToken(memberId, "", List.of());
			SecurityContextHolder.getContext().setAuthentication(authentication);

			MDC.put(MEMBER_ID, String.valueOf(memberId));
			request.setAttribute(MEMBER_ID, memberId);

			return true;
		} catch (ExpiredJwtException e) {
			log.debug(
				"access token 만료 method={} path={}",
				request.getMethod(),
				request.getRequestURI()
			);

			entrypoint.commence(
				request, response,
				new CredentialsExpiredException("Access Token Expired")
			);
		} catch (JwtException | IllegalArgumentException e) {
			log.warn(
				"유효하지 않은 access token method={} path={}",
				request.getMethod(),
				request.getRequestURI()
			);

			entrypoint.commence(
				request, response,
				new BadCredentialsException("Invalid Access Token")
			);
		}

		return false;
	}
}
