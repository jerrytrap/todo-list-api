package com.example.todolistapi.todo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todolistapi.common.response.ApiResponse;
import com.example.todolistapi.common.page.PageResponse;
import com.example.todolistapi.todo.dto.TodoRequest;
import com.example.todolistapi.todo.dto.TodoResponse;
import com.example.todolistapi.todo.dto.TodoSearchRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/todos")
public class TodoController {
	private final TodoService todoService;

	@PostMapping
	public ApiResponse<TodoResponse> create(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody TodoRequest request
	){
		return ApiResponse.success(HttpStatus.CREATED, todoService.create(memberId, request));
	}

	@PutMapping("/{todoId}")
	public ApiResponse<TodoResponse> update(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody TodoRequest request,
		@PathVariable Long todoId
	) {
		return ApiResponse.success(HttpStatus.OK, todoService.update(memberId, todoId, request));
	}

	@DeleteMapping("/{todoId}")
	public ApiResponse<Void> delete(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long todoId
	) {
		todoService.delete(memberId, todoId);
		return ApiResponse.success(HttpStatus.NO_CONTENT);
	}

	@GetMapping
	public ApiResponse<PageResponse<TodoResponse>> findAll(
		@AuthenticationPrincipal Long memberId,
		@Valid @ModelAttribute TodoSearchRequest request
	) {
		return ApiResponse.success(
			HttpStatus.OK,
			todoService.findAll(memberId, request)
		);
	}
}
