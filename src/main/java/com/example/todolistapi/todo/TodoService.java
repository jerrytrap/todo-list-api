package com.example.todolistapi.todo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.exception.ServiceException;
import com.example.todolistapi.member.Member;
import com.example.todolistapi.member.MemberRepository;
import com.example.todolistapi.todo.dto.TodoRequest;
import com.example.todolistapi.todo.dto.TodoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
	private final TodoRepository todoRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public TodoResponse create(Long userId, TodoRequest request) {
		Member owner = memberRepository.findById(userId)
			.orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

		Todo todo = Todo.builder()
			.owner(owner)
			.title(request.title())
			.description(request.description())
			.build();

		return TodoResponse.from(todoRepository.save(todo));
	}

	@Transactional
	public TodoResponse update(Long memberId, Long todoId, TodoRequest request) {
		Todo todo = getOwnedTodo(memberId, todoId);

		todo.update(request.title(), request.description());

		return TodoResponse.from(todo);
	}

	@Transactional
	public void delete(Long memberId, Long todoId) {
		Todo todo = getOwnedTodo(memberId, todoId);

		todoRepository.delete(todo);
	}

	@Transactional(readOnly = true)
	public List<TodoResponse> findAll(Long memberId) {
		return todoRepository.findByOwnerId(memberId)
			.stream()
			.map(todo -> TodoResponse.from(todo))
			.toList();
	}

	private Todo getOwnedTodo(Long memberId, Long todoId) {
		Todo todo = todoRepository.findById(todoId)
			.orElseThrow(() -> new ServiceException(ErrorCode.TODO_NOT_FOUND));

		if (!todo.isOwnedBy(memberId)) {
			throw new ServiceException(ErrorCode.FORBIDDEN);
		}

		return todo;
	}
}
