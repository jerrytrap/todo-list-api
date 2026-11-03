package com.example.todolistapi.common.exception;

import java.util.stream.Collectors;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.todolistapi.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ServiceException.class)
	public ApiResponse<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
		log.warn("요청 처리 실패 code={} path={}", e.getErrorCode(), request.getRequestURI());
		return ApiResponse.error(e.getErrorCode());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ApiResponse<Void> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e,
		HttpServletRequest request
	) {
		String fields = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(FieldError::getField)
			.distinct()
			.collect(Collectors.joining(","));

		log.warn("입력 검증 실패 fields={} path={}", fields, request.getRequestURI());
		return ApiResponse.error(ErrorCode.INVALID_INPUT);
	}

	@ExceptionHandler(Exception.class)
	public ApiResponse<Void> handleUnexpected(Exception e, HttpServletRequest request) {
		log.error("예상하지 못한 오류 path={}", request.getRequestURI(), e);
		return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
	}
}
