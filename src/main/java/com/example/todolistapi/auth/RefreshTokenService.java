package com.example.todolistapi.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RefreshTokenService {
	private final StringRedisTemplate redisTemplate;
	private final long refreshTokenExpirationSeconds;
	private static final String KEY_PREFIX = "refresh-token:";

	public RefreshTokenService(
		StringRedisTemplate redisTemplate,
		@Value("${auth.refresh-token-expiration}") long refreshTokenExpirationSeconds
	) {
		this.redisTemplate = redisTemplate;
		this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
	}

	public String generateToken(Long userId) {
		String refreshToken = UUID.randomUUID().toString();
		String key = KEY_PREFIX + hashToken(refreshToken);

		redisTemplate.opsForValue().set(
			key,
			userId.toString(),
			Duration.ofMillis(refreshTokenExpirationSeconds * 1000)
		);

		return refreshToken;
	}

	public Long validateAndRevokeToken(String refreshToken) {
		String key = KEY_PREFIX + hashToken(refreshToken);
		String userId = redisTemplate.opsForValue().getAndDelete(key);

		if (userId == null) {
			log.debug("잘못된 Refresh Token으로 요청");
			throw new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		return Long.parseLong(userId);
	}

	public void revokeToken(String refreshToken) {
		String key = KEY_PREFIX + hashToken(refreshToken);
		redisTemplate.delete(key);
	}

	private String hashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}
