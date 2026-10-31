package com.example.todolistapi.common.response;

import org.springframework.http.HttpStatus;

import com.example.todolistapi.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
	private final int status;
	private final String code;
	private final String message;
	private final T data;

	public static <T> ApiResponse<T> success(HttpStatus status, T data) {
		return new ApiResponse<>(status.value(), null, "Success", data);
	}

	public static ApiResponse<Void> success(HttpStatus status) {
		return new ApiResponse<>(status.value(), null,"Success", null);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), null);
	}
}
