package com.example.todolistapi.auth.dto;

public record LogoutRequest(
	String refreshToken
) {}
