package com.example.todolistapi.todo;

public record TodoRequest (
	String title,
	String description
) {}
