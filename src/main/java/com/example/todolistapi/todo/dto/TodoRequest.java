package com.example.todolistapi.todo.dto;

import com.example.todolistapi.todo.TodoStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoRequest (
	TodoStatus status,

	@NotBlank
	@Size(max = 100)
	String title,

	@Size(max = 500)
	String description
) {}
