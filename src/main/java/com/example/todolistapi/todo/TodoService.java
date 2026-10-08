package com.example.todolistapi.todo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
	private final TodoRepository todoRepository;

	@Transactional
	public TodoResponse create(TodoRequest request) {
		Todo todo = Todo.builder()
			.title(request.title())
			.description(request.description())
			.build();

		return TodoResponse.from(todoRepository.save(todo));
	}

	@Transactional
	public TodoResponse update(Long id, TodoRequest request) {
		Todo todo = todoRepository.findById(id)
			.orElseThrow(() -> new RuntimeException());

		todo.setTitle(request.title());
		todo.setDescription(request.description());

		return TodoResponse.from(todo);
	}

	@Transactional
	public void delete(Long id) {
		Todo todo = todoRepository.findById(id)
			.orElseThrow(() -> new RuntimeException());

		todoRepository.delete(todo);
	}

	@Transactional(readOnly = true)
	public List<TodoResponse> findAll() {
		return todoRepository.findAll()
			.stream()
			.map(todo -> TodoResponse.from(todo))
			.toList();
	}
}
