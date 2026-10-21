package com.example.todolistapi.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.todolistapi.auth.dto.LoginRequest;
import com.example.todolistapi.auth.dto.LogoutRequest;
import com.example.todolistapi.auth.dto.RefreshRequest;
import com.example.todolistapi.auth.dto.RegisterRequest;
import com.example.todolistapi.auth.dto.TokenResponse;
import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.exception.ServiceException;
import com.example.todolistapi.security.JwtTokenProvider;
import com.example.todolistapi.member.Member;
import com.example.todolistapi.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;

	public TokenResponse register(RegisterRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new ServiceException(ErrorCode.DUPLICATE_EMAIL);
		}

		Member member = Member.builder()
			.name(request.name())
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.build();

		Member saved = memberRepository.save(member);
		String accessToken = jwtTokenProvider.generateToken(saved.getId());
		String refreshToken = refreshTokenService.generateToken(saved.getId());

		return new TokenResponse(accessToken, refreshToken);
	}

	public TokenResponse login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
			.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}

		String accessToken = jwtTokenProvider.generateToken(member.getId());
		String refreshToken = refreshTokenService.generateToken(member.getId());

		return new TokenResponse(accessToken, refreshToken);
	}

	public TokenResponse refresh(RefreshRequest request) {
		String refreshToken = request.refreshToken();
		Long userId = refreshTokenService.validateAndRevokeToken(refreshToken);

		String accessToken = jwtTokenProvider.generateToken(userId);
		String newRefreshToken = refreshTokenService.generateToken(userId);

		return new TokenResponse(accessToken, newRefreshToken);
	}

	public void logout(LogoutRequest request) {
		if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
			return;
		}

		refreshTokenService.revokeToken(request.refreshToken());
	}
}
