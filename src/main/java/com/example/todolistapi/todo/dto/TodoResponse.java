package com.example.todolistapi.todo.dto;

import java.time.LocalDateTime;

import com.example.todolistapi.todo.Todo;
import com.example.todolistapi.todo.TodoStatus;

public record TodoResponse (
	Long id,
	TodoStatus status,
	String title,
	String description,
	LocalDateTime createdAt
) {
	public static TodoResponse from(Todo todo) {
		return new TodoResponse(
			todo.getId(),
			todo.getStatus(),
			todo.getTitle(),
			todo.getDescription(),
			todo.getCreatedAt()
		);
	}
}
