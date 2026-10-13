package com.example.todolistapi;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.todolistapi.security.JwtTokenProvider;

class JwtTokenProviderTest {
	private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key";
	private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000L);

	@Test
	@DisplayName("토큰 검증_성공")
	void validateToken_success() {
		String token = provider.generateToken(1L);

		assertThat(provider.validateToken(token)).isTrue();
	}

	@Test
	@DisplayName("토큰 생성_ID 확인 가능")
	void getUserId_success() {
		String token = provider.generateToken(1L);

		assertThat(provider.getUserId(token)).isEqualTo(1L);
	}

	@Test
	@DisplayName("토큰 검증_만료된 토큰_실패")
	void validateToken_expiredToken_fail() {
		JwtTokenProvider expired = new JwtTokenProvider(SECRET, -1000L);  // 이미 만료
		String token = expired.generateToken(1L);

		assertThat(provider.validateToken(token)).isFalse();
	}

	@Test
	@DisplayName("토큰 검증_위조된 토큰_실패")
	void validateToken_tamperedToken_fail() {
		String token = provider.generateToken(1L);
		String tampered = token.substring(0, token.length() - 2) + "xx";  // signature 변조

		assertThat(provider.validateToken(tampered)).isFalse();
	}

	@Test
	@DisplayName("토큰 검증_잘못된 키로 서명된 토큰_검증 실패")
	void validateToken_wrongSecretKey_fail() {
		JwtTokenProvider other = new JwtTokenProvider("wrong-secret-key-wrong-secret-key-wrong-secret-key", 60_000L);
		String token = other.generateToken(1L);

		assertThat(provider.validateToken(token)).isFalse();
	}

	@Test
	@DisplayName("토큰 검증_형식이 잘못된 토큰_검증 실패")
	void validateToken_illegalArgument_fail() {
		assertThat(provider.validateToken("not.a.jwt")).isFalse();
		assertThat(provider.validateToken("")).isFalse();
		assertThat(provider.validateToken(null)).isFalse();
	}
}