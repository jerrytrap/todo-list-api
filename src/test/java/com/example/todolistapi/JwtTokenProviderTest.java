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
	@DisplayName("토큰 생성_성공")
	void getUserId_success() {
		String token = provider.generateToken(1L);

		assertThat(provider.getUserId(token)).isEqualTo(1L);
	}

	@Test
	@DisplayName("토큰 추출_성공")
	void resolveToken_success() {
		String header = "Bearer sample.jwt.token";

		assertThat(provider.resolveToken(header)).isEqualTo("sample.jwt.token");
	}

	@Test
	@DisplayName("토큰 추출_실패_잘못된 헤더")
	void resolveToken_fail() {
		String withoutPrefixHeader = "sample.jwt.token";

		assertThat(provider.resolveToken(null)).isNull();
		assertThat(provider.resolveToken(withoutPrefixHeader)).isNull();
	}

	@Test
	@DisplayName("토큰 검증_실패_만료된 토큰")
	void validateToken_fail_expiredToken() {
		JwtTokenProvider expired = new JwtTokenProvider(SECRET, -1000L);
		String token = expired.generateToken(1L);

		assertThat(provider.validateToken(token)).isFalse();
	}

	@Test
	@DisplayName("토큰 검증_실패_잘못된 키로 서명된 토큰")
	void validateToken_fail_wrongSecretKey() {
		JwtTokenProvider other = new JwtTokenProvider("wrong-secret-key-wrong-secret-key-wrong-secret-key", 60_000L);
		String token = other.generateToken(1L);

		assertThat(provider.validateToken(token)).isFalse();
	}

	@Test
	@DisplayName("토큰 검증_실패_형식이 잘못된 토큰")
	void validateToken_fail_illegalArgument() {
		assertThat(provider.validateToken("wrong.jwt.token")).isFalse();
		assertThat(provider.validateToken("")).isFalse();
		assertThat(provider.validateToken(null)).isFalse();
	}
}