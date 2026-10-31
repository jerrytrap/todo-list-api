package com.example.todolistapi.auth;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieProvider {
	private final long refreshTokenExpirationSeconds;
	private final boolean secure;
	private final String sameSite;
	private static final String COOKIE_NAME = "refreshToken";
	private static final String COOKIE_PATH = "/api/v1/auth";

	public RefreshTokenCookieProvider(
		@Value("${auth.refresh-token-expiration}") long refreshTokenExpirationSeconds,
		@Value("${app.cookie.secure}") boolean secure,
		@Value("${app.cookie.same-site}") String sameSite
	) {
		this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
		this.secure = secure;
		this.sameSite = sameSite;
	}

	public ResponseCookie create(String refreshToken) {
		return ResponseCookie.from(COOKIE_NAME, refreshToken)
			.httpOnly(true)
			.secure(secure)
			.sameSite(sameSite)
			.path(COOKIE_PATH)
			.maxAge(Duration.ofSeconds(refreshTokenExpirationSeconds))
			.build();
	}

	public ResponseCookie expire() {
		return ResponseCookie.from(COOKIE_NAME, "")
			.httpOnly(true)
			.secure(secure)
			.sameSite(sameSite)
			.path(COOKIE_PATH)
			.maxAge(0)
			.build();
	}
}
