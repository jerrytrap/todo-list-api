package com.example.todolistapi.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todolistapi.auth.dto.LoginRequest;
import com.example.todolistapi.auth.dto.RegisterRequest;
import com.example.todolistapi.auth.dto.TokenPair;
import com.example.todolistapi.auth.dto.TokenResponse;
import com.example.todolistapi.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService authService;
	private final RefreshTokenCookieProvider cookieProvider;

	@PostMapping("/register")
	public ApiResponse<TokenResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
		TokenPair tokens = authService.register(request);
		response.setHeader(HttpHeaders.SET_COOKIE, cookieProvider.create(tokens.refreshToken()).toString());

		return ApiResponse.success(HttpStatus.OK, new TokenResponse(tokens.accessToken()));
	}

	@PostMapping("/login")
	public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		TokenPair tokens = authService.login(request);
		response.setHeader(HttpHeaders.SET_COOKIE, cookieProvider.create(tokens.refreshToken()).toString());

		return ApiResponse.success(HttpStatus.OK, new TokenResponse(tokens.accessToken()));
	}

	@PostMapping("/refresh")
	public ApiResponse<TokenResponse> refresh(
		@CookieValue(name = "refreshToken", required = false) String refreshToken,
		HttpServletResponse response
	) {
		TokenPair tokens = authService.refresh(refreshToken);
		response.setHeader(HttpHeaders.SET_COOKIE, cookieProvider.create(tokens.refreshToken()).toString());

		return ApiResponse.success(HttpStatus.OK, new TokenResponse(tokens.accessToken()));
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(
		@CookieValue(name = "refreshToken", required = false) String refreshToken,
		HttpServletResponse response
	) {
		authService.logout(refreshToken);
		response.setHeader(HttpHeaders.SET_COOKIE, cookieProvider.expire().toString());

		return ApiResponse.success((HttpStatus.NO_CONTENT));
	}
}
