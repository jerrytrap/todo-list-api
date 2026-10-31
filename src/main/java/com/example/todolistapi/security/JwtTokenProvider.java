package com.example.todolistapi.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	private final SecretKey key;
	private final long accessTokenExpirationSeconds;
	private static final String BEARER_PREFIX = "Bearer ";

	public JwtTokenProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${auth.jwt.access-token-expiration}") long accessTokenExpirationSeconds
	) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
	}

	public String generateToken(Long userId) {
		Claims claims = Jwts.claims()
			.subject(String.valueOf(userId))
			.build();

		Date now = new Date();

		return Jwts.builder()
			.claims(claims)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + accessTokenExpirationSeconds * 1000L))
			.signWith(key)
			.compact();
	}

	public TokenStatus validateToken(String token) {
		try {
			parseClaims(token);
			return TokenStatus.VALID;
		} catch (ExpiredJwtException e) {
			return TokenStatus.EXPIRED;
		} catch(JwtException | IllegalArgumentException e) {
			return TokenStatus.INVALID;
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
