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
	private final static int DEFAULT_PAGE_NUMBER = 1;
	private final static int DEFAULT_PAGE_LIMIT = 10;
	private final static TodoSort DEFAULT_SORT_BY = TodoSort.CREATED;

	public TodoSearchRequest {
		if (page == null) {
			page = DEFAULT_PAGE_NUMBER;
		}

		if (limit == null) {
			limit = DEFAULT_PAGE_LIMIT;
		}

		if (sort == null) {
			sort = DEFAULT_SORT_BY;
		}

		if (order == null) {
			order = sort.equals(TodoSort.CREATED) ? SortDirection.DESC : SortDirection.ASC;
		}
	}
}
