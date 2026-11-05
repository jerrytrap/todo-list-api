package com.example.todolistapi.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
			.orElseThrow(() -> {
				log.debug("로그인 실패: 존재하지 않는 이메일");
				return new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			});

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			log.debug("로그인 실패: 비밀번호 불일치 memberId={}", member.getId());
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}

		String accessToken = jwtTokenProvider.generateToken(member.getId());
		String refreshToken = refreshTokenService.generateToken(member.getId());

		log.info("로그인 성공 memberId={}", member.getId());
		return new TokenPair(accessToken, refreshToken);
	}

	public TokenPair refresh(String refreshToken) {
		if (!StringUtils.hasText(refreshToken)) {
			log.debug("Refresh Token이 없는 갱신 요청");
			throw new ServiceException(ErrorCode.EMPTY_REFRESH_TOKEN);
		}

		long totalStartTime = System.nanoTime();
		long validateStartTime = System.nanoTime();
		Long memberId = refreshTokenService.validateAndRevokeToken(refreshToken);
		long validateDuration = (System.nanoTime() - validateStartTime) / 1_000_000;

		long accessTokenStartTime = System.nanoTime();
		String accessToken = jwtTokenProvider.generateToken(memberId);
		long accessTokenDuration = (System.nanoTime() - accessTokenStartTime) / 1_000_000;

		long refreshTokenStartTime = System.nanoTime();
		String newRefreshToken = refreshTokenService.generateToken(memberId);
		long refreshTokenDuration = (System.nanoTime() - refreshTokenStartTime) / 1_000_000;
		long totalDuration = (System.nanoTime() - totalStartTime) / 1_000_000;

		log.info(
			"PERF token-refresh total={}ms validate={}ms access-jwt={}ms refresh-store={}ms",
			totalDuration,
			validateDuration,
			accessTokenDuration,
			refreshTokenDuration
		);

		return new TokenPair(accessToken, newRefreshToken);
	}

	public void logout(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return;
		}

		refreshTokenService.revokeToken(refreshToken);
	}
}
