package com.example.todolistapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
	@NotBlank
	@Size(max = 10)
	String name,

	@Email
	@NotBlank
	String email,

	@NotBlank
	@Size(min = 8)
	String password
) {}
