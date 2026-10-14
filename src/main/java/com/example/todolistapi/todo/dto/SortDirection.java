package com.example.todolistapi.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortDirection {
	ASC("asc"),
	DESC("desc");

	private final String property;
}
