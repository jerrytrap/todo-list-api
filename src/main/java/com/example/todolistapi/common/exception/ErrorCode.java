package com.example.todolistapi.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid input"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Duplicate Email"),
	USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden");

	private final HttpStatus status;
	private final String message;
}
