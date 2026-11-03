package com.example.todolistapi.security;

import static com.example.todolistapi.common.logging.MdcKey.*;

import java.io.IOException;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
		String token = jwtTokenProvider.resolveToken(request.getHeader(AUTHORIZATION_HEADER));

		if (token != null) {
			TokenStatus status = jwtTokenProvider.validateToken(token);

			switch (status) {
				case VALID -> {
					Long memberId = jwtTokenProvider.getUserId(token);

					Authentication authentication = new UsernamePasswordAuthenticationToken(memberId, "", List.of());
					SecurityContextHolder.getContext().setAuthentication(authentication);

					MDC.put(MEMBER_ID, String.valueOf(memberId));
					request.setAttribute(MEMBER_ID, memberId);
				}
				case EXPIRED -> {
					log.debug("access token 만료");
					entrypoint.commence(request, response, new CredentialsExpiredException("Access Token Expired"));
					return;
				}
				case INVALID -> {
					log.debug("유효하지 않은 access token path={}", request.getRequestURI());
				}
			}
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MEMBER_ID);
		}
	}
}
