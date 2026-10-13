package com.example.todolistapi.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.todolistapi.auth.dto.LoginRequest;
import com.example.todolistapi.auth.dto.RegisterRequest;
import com.example.todolistapi.auth.dto.TokenResponse;
import com.example.todolistapi.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class AuthController {
	private final AuthService authService;

	@PostMapping("/register")
	public ApiResponse<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ApiResponse.success(HttpStatus.OK, authService.register(request));
	}

	@PostMapping("/login")
	public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.success(HttpStatus.OK, authService.login(request));
	}
}
