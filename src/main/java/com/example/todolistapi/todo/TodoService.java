package com.example.todolistapi.todo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.exception.ServiceException;
import com.example.todolistapi.common.page.PageResponse;
import com.example.todolistapi.member.Member;
import com.example.todolistapi.member.MemberRepository;
import com.example.todolistapi.todo.dto.TodoRequest;
import com.example.todolistapi.todo.dto.TodoResponse;
import com.example.todolistapi.todo.dto.TodoSearchRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {
	private final TodoRepository todoRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public TodoResponse create(Long memberId, TodoRequest request) {
		Member owner = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

		Todo todo = Todo.builder()
			.owner(owner)
			.title(request.title())
			.description(request.description())
			.build();

		Todo saved = todoRepository.save(todo);
		log.info("할 일 생성 todoId={}", saved.getId());
		return TodoResponse.from(saved);
	}

	@Transactional
	public TodoResponse update(Long memberId, Long todoId, TodoRequest request) {
		Todo todo = getOwnedTodo(memberId, todoId);
		todo.update(request.status(), request.title(), request.description());

		log.info("할 일 수정 todoId={}", todoId);
		return TodoResponse.from(todo);
	}

	@Transactional
	public void delete(Long memberId, Long todoId) {
		Todo todo = getOwnedTodo(memberId, todoId);

		todoRepository.delete(todo);
		log.info("할 일 삭제 todoId={}", todoId);
	}

	@Transactional(readOnly = true)
	public PageResponse<TodoResponse> findAll(Long memberId, TodoSearchRequest request) {
		Sort.Direction direction = Sort.Direction.fromString(request.order().getProperty());
		String sortBy = request.sort().getProperty();
		Sort sort = Sort.by(direction, sortBy);
		PageRequest pageRequest = PageRequest.of(request.page() - 1, request.limit(), sort);

		Page<TodoResponse> result = todoRepository.searchByOwnerId(memberId, request.status(), pageRequest)
			.map(TodoResponse::from);

		log.debug("할 일 목록 조회 page={} limit={} sort={} order={}",
			request.page(), request.limit(), request.sort(), request.order());
		return PageResponse.from(result);
	}

	private Todo getOwnedTodo(Long memberId, Long todoId) {
		Todo todo = todoRepository.findById(todoId)
			.orElseThrow(() -> new ServiceException(ErrorCode.TODO_NOT_FOUND));

		if (!todo.isOwnedBy(memberId)) {
			log.debug("권한이 없는 Todo 요청 memberId={}, todoId={}", memberId, todoId);
			throw new ServiceException(ErrorCode.FORBIDDEN);
		}

		return todo;
	}
}
