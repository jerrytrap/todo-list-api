package com.example.todolistapi.todo;

public record TodoResponse (
	Long id,
	String title,
	String description
) {
	public static TodoResponse from(Todo todo) {
		return new TodoResponse(
			todo.getId(),
			todo.getTitle(),
			todo.getDescription()
		);
	}
}
