package com.example.todolistapi.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TodoSort {
	CREATED("createdAt"),
	TITLE("title");

	private final String property;
}
