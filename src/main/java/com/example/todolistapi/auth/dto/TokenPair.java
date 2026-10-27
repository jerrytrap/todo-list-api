package com.example.todolistapi.auth.dto;

public record TokenPair(
	String accessToken,
	String refreshToken
) {}
