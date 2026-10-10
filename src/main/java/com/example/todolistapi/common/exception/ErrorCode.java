package com.example.todolistapi.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid input");

	private final HttpStatus status;
	private final String message;
}
