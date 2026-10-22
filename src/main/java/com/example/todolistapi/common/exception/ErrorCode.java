package com.example.todolistapi.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	// Common
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid input"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),

	// Todo
	TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "Todo not found"),

	// Member
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found"),

	// Auth
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Duplicate Email"),
	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access token expired"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid refresh token");

	private final HttpStatus status;
	private final String message;
}
