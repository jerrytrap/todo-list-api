package com.example.todolistapi.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenProvider {
	private final SecretKey key;
	private final long expirationMs;
	private static final String BEARER_PREFIX = "Bearer ";

	public JwtTokenProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateToken(Long userId) {
		Claims claims = Jwts.claims()
			.subject(String.valueOf(userId))
			.build();

		Date now = new Date();

		return Jwts.builder()
			.claims(claims)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + expirationMs))
			.signWith(key)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public long getUserId(String token) {
		return Long.parseLong(parseClaims(token).getSubject());
	}

	public String resolveToken(String header) {
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			return header.substring(BEARER_PREFIX.length());
		}

		return null;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
