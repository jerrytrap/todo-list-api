package com.example.todolistapi.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.todolistapi.auth.dto.LoginRequest;
import com.example.todolistapi.auth.dto.RegisterRequest;
import com.example.todolistapi.auth.dto.TokenPair;
import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.exception.ServiceException;
import com.example.todolistapi.security.JwtTokenProvider;
import com.example.todolistapi.member.Member;
import com.example.todolistapi.member.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;

	public TokenPair register(RegisterRequest request) {
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

		log.info("회원가입 완료 memberId={}", member.getId());
		return new TokenPair(accessToken, refreshToken);
	}

	public TokenPair login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
			.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}

		String accessToken = jwtTokenProvider.generateToken(member.getId());
		String refreshToken = refreshTokenService.generateToken(member.getId());

		log.info("로그인 성공 memberId={}", member.getId());
		return new TokenPair(accessToken, refreshToken);
	}

	public TokenPair refresh(String refreshToken) {
		Long memberId = refreshTokenService.validateAndRevokeToken(refreshToken);

		String accessToken = jwtTokenProvider.generateToken(memberId);
		String newRefreshToken = refreshTokenService.generateToken(memberId);

		log.info("토큰 재발급 memberId={}", memberId);
		return new TokenPair(accessToken, newRefreshToken);
	}

	public void logout(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return;
		}

		refreshTokenService.revokeToken(refreshToken);
	}
}
