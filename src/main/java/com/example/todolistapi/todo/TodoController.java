package com.example.todolistapi.todo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todolistapi.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/todos")
public class TodoController {
	private final TodoService todoService;

	@PostMapping
	public ApiResponse<TodoResponse> create(@RequestBody TodoRequest request){
		return ApiResponse.success(HttpStatus.CREATED, todoService.create(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<TodoResponse> update(@RequestBody TodoRequest request, @PathVariable Long id) {
		return ApiResponse.success(HttpStatus.OK, todoService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		todoService.delete(id);
		return ApiResponse.success(HttpStatus.NO_CONTENT);
	}

	@GetMapping
	public ApiResponse<List<TodoResponse>> findAll() {
		return ApiResponse.success(HttpStatus.OK, todoService.findAll());
	}
}
