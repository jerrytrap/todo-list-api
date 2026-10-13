package com.example.todolistapi.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.todolistapi.auth.dto.LoginRequest;
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
		return new TokenResponse(jwtTokenProvider.generateToken(saved.getId()));
	}

	public TokenResponse login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
			.orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new ServiceException(ErrorCode.INVALID_PASSWORD);
		}

		return new TokenResponse(jwtTokenProvider.generateToken(member.getId()));
	}
}
