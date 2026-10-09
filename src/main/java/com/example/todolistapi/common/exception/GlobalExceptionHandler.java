package com.example.todolistapi.common.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.todolistapi.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ServiceException.class)
	public ApiResponse<Void> handleServiceException(ServiceException e) {
		return ApiResponse.error(e.getErrorCode());
	}
}
