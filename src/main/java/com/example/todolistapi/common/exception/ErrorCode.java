package com.example.todolistapi.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	// Common
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid input"),
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),

	// Todo
	TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "Todo not found"),

	// Member
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found"),

	// Auth
	// 401
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access token expired"),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid Access Token"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid refresh token"),
	EMPTY_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Empty refresh token"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Duplicate Email");

	private final HttpStatus status;
	private final String message;
}
