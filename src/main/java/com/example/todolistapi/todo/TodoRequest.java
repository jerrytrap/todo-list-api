package com.example.todolistapi.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoRequest (
	@NotBlank
	@Size(max = 100)
	String title,

	@Size(max = 500)
	String description
) {}
