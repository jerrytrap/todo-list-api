package com.example.todolistapi;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.todolistapi.auth.AuthService;
import com.example.todolistapi.auth.RefreshTokenService;
import com.example.todolistapi.auth.dto.LoginRequest;
import com.example.todolistapi.auth.dto.RegisterRequest;
import com.example.todolistapi.auth.dto.TokenPair;
import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.exception.ServiceException;
import com.example.todolistapi.member.Member;
import com.example.todolistapi.member.MemberRepository;
import com.example.todolistapi.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
	@Mock
	private MemberRepository memberRepository;

	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("회원가입_성공")
	void register_success() {
		// given
		RegisterRequest request = new RegisterRequest("tester", "tester@test.com", "raw-password");

		when(memberRepository.existsByEmail("tester@test.com"))
			.thenReturn(false);

		when(passwordEncoder.encode("raw-password"))
			.thenReturn("encoded-password");

		when(memberRepository.save(any(Member.class)))
			.thenAnswer(inv -> {
				Member m = inv.getArgument(0);
				ReflectionTestUtils.setField(m, "id", 1L);
				return m;
			});

		when(jwtTokenProvider.generateToken(1L))
			.thenReturn("access");

		when(refreshTokenService.generateToken(1L))
			.thenReturn("refresh");

		// when
		TokenPair response = authService.register(request);

		// then
		ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(captor.capture());
		Member saved = captor.getValue();

		assertThat(saved.getPassword()).isEqualTo("encoded-password");
		assertThat(saved.getName()).isEqualTo("tester");
		assertThat(saved.getEmail()).isEqualTo("tester@test.com");

		assertThat(response.accessToken()).isEqualTo("access");
		assertThat(response.refreshToken()).isEqualTo("refresh");
	}

	@Test
	@DisplayName("회원가입_실패_이메일 중복")
	void register_fail_duplicateEmail() {
		// given
		RegisterRequest request = new RegisterRequest("tester", "tester@test.com", "raw-password");

		when(memberRepository.existsByEmail("tester@test.com"))
			.thenReturn(true);

		// when, then
		assertThatThrownBy(() -> authService.register(request))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.DUPLICATE_EMAIL);

		verify(memberRepository, never()).save(any(Member.class));
		verify(jwtTokenProvider, never()).generateToken(any());
		verify(refreshTokenService, never()).generateToken(any());
	}

	@Test
	@DisplayName("로그인_성공")
	void login_success() {
		// given
		Member member = getMemberWithId(1L, "encoded-password");
		LoginRequest request = new LoginRequest("tester@test.com", "raw-password");

		when(memberRepository.findByEmail("tester@test.com"))
			.thenReturn(Optional.of(member));

		when(passwordEncoder.matches("raw-password", "encoded-password"))
			.thenReturn(true);

		when(jwtTokenProvider.generateToken(1L))
			.thenReturn("access");

		when(refreshTokenService.generateToken(1L))
			.thenReturn("refresh");

		// when
		TokenPair response = authService.login(request);

		// then
		assertThat(response.accessToken()).isEqualTo("access");
		assertThat(response.refreshToken()).isEqualTo("refresh");
	}

	@Test
	@DisplayName("로그인_실패_존재하지 않는 이메일")
	void login_fail_emailNotFound() {
		// given
		LoginRequest request = new LoginRequest("tester@test.com", "raw-password");

		when(memberRepository.findByEmail("tester@test.com"))
			.thenReturn(Optional.empty());

		// when, then
		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.INVALID_CREDENTIALS);

		verify(jwtTokenProvider, never()).generateToken(any());
		verify(refreshTokenService, never()).generateToken(any());
	}

	@Test
	@DisplayName("로그인_실패_틀린 비밀번호")
	void login_fail_passwordNotMatch() {
		// given
		LoginRequest request = new LoginRequest("tester@test.com", "wrong-password");

		when(memberRepository.findByEmail("tester@test.com"))
			.thenReturn(Optional.of(getMemberWithId(1L, "encoded-password")));

		when(passwordEncoder.matches("wrong-password", "encoded-password"))
			.thenReturn(false);

		// when, then
		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.INVALID_CREDENTIALS);

		verify(jwtTokenProvider, never()).generateToken(any());
		verify(refreshTokenService, never()).generateToken(any());
	}

	@Test
	@DisplayName("Access Token 재발급_성공")
	void refresh_success() {
		// given
		when(refreshTokenService.validateAndRevokeToken("old-refresh"))
			.thenReturn(1L);

		when(jwtTokenProvider.generateToken(1L))
			.thenReturn("new-access");

		when(refreshTokenService.generateToken(1L))
			.thenReturn("new-refresh");

		// when
		TokenPair response = authService.refresh("old-refresh");

		// then
		assertThat(response.accessToken()).isEqualTo("new-access");
		assertThat(response.refreshToken()).isEqualTo("new-refresh");
	}

	@Test
	@DisplayName("Access Token 재발급_실패_유효하지 않은 Refresh Token")
	void refresh_fail_invalidRefreshToken() {
		// given
		when(refreshTokenService.validateAndRevokeToken("wrong-refresh"))
			.thenThrow(new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN));

		// when, then
		assertThatThrownBy(() -> authService.refresh("wrong-refresh"))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);

		verify(jwtTokenProvider, never()).generateToken(any());
		verify(refreshTokenService, never()).generateToken(any());
	}

	@Test
	@DisplayName("로그아웃_성공_Refresh Token 삭제")
	void logout_success_refreshTokenExists() {
		// when
		authService.logout("refresh");

		// then
		verify(refreshTokenService).revokeToken("refresh");
	}

	private Member getMemberWithId(Long id, String encodedPassword) {
		Member member = new Member("tester", "tester@test.com", encodedPassword);
		ReflectionTestUtils.setField(member, "id", id);

		return member;
	}
}
