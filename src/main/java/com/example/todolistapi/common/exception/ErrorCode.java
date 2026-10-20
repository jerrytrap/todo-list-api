package com.example.todolistapi.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	// Common
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid input"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),

	// Todo
	TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "Todo Not Found"),

	// Member
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member Not Found"),

	// Auth
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
	MEMBER_NOT_EXIST(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Duplicate Email"),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access Token Expired"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");

	private final HttpStatus status;
	private final String message;
}
