package com.example.todolistapi.todo.dto;

import com.example.todolistapi.todo.TodoStatus;

import jakarta.validation.constraints.Min;

public record TodoSearchRequest(
	@Min(1)
	Integer page,

	@Min(1)
	Integer limit,

	TodoStatus status,

	TodoSort sort,

	SortDirection order
) {
	public TodoSearchRequest {
		if (page == null) {
			page = 1;
		}

		if (limit == null) {
			limit = 10;
		}

		if (sort == null) {
			sort = TodoSort.CREATED;
		}

		if (order == null) {
			order = sort.equals(TodoSort.CREATED) ? SortDirection.DESC : SortDirection.ASC;
		}
	}
}
