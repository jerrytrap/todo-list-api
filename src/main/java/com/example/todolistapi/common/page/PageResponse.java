package com.example.todolistapi.common.page;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponse<T> (
	List<T> result,
	int page,
	int limit,
	int total
) {
	public static <T> PageResponse<T> from(Page<T> result) {
		return new PageResponse<>(
			result.getContent(),
			result.getNumber() + 1,
			result.getSize(),
			result.getTotalPages()
		);
	}
}
