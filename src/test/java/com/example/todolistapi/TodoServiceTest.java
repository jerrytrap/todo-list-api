package com.example.todolistapi;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.todolistapi.common.exception.ErrorCode;
import com.example.todolistapi.common.exception.ServiceException;
import com.example.todolistapi.common.page.PageResponse;
import com.example.todolistapi.member.Member;
import com.example.todolistapi.member.MemberRepository;
import com.example.todolistapi.todo.Todo;
import com.example.todolistapi.todo.TodoRepository;
import com.example.todolistapi.todo.TodoService;
import com.example.todolistapi.todo.TodoStatus;
import com.example.todolistapi.todo.dto.SortDirection;
import com.example.todolistapi.todo.dto.TodoRequest;
import com.example.todolistapi.todo.dto.TodoResponse;
import com.example.todolistapi.todo.dto.TodoSearchRequest;
import com.example.todolistapi.todo.dto.TodoSort;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
	@Mock
	private TodoRepository todoRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private TodoService todoService;

	@Test
	@DisplayName("Todo 생성_성공")
	void create_success() {
		// given
		Long memberId = 1L;
		TodoRequest request = new TodoRequest(TodoStatus.READY, "test title", "test description");
		Member member = getMemberWithId(memberId);

		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(member));

		when(todoRepository.save(any(Todo.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		// when
		TodoResponse response = todoService.create(memberId, request);

		// then
		ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
		verify(todoRepository).save(captor.capture());
		Todo saved = captor.getValue();

		assertThat(saved.getOwner()).isSameAs(member);
		assertThat(saved.getTitle()).isEqualTo("test title");
		assertThat(saved.getDescription()).isEqualTo("test description");

		assertThat(response.title()).isEqualTo("test title");
		assertThat(response.description()).isEqualTo("test description");
	}

	@Test
	@DisplayName("Todo 생성_실패_존재하지 않는 사용자")
	void create_fail_memberNotFound() {
		// given
		Long memberId = 1L;
		TodoRequest request = new TodoRequest(TodoStatus.READY, "test title", "test description");

		when(memberRepository.findById(memberId))
			.thenReturn(Optional.empty());

		// when, then
		assertThatThrownBy(() -> todoService.create(memberId, request))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

		verify(todoRepository, never()).save(any(Todo.class));
	}

	@Test
	@DisplayName("Todo 수정_성공")
	void update_success() {
		// given
		Long memberId = 1L;
		Long todoId = 1L;
		Member member = getMemberWithId(memberId);
		Todo todo = getTodoWithId(todoId, member);
		TodoRequest request = new TodoRequest(TodoStatus.READY, "new title", "new description");

		when(todoRepository.findById(todoId))
			.thenReturn(Optional.of(todo));

		// when
		TodoResponse response = todoService.update(memberId, todoId, request);

		// then
		assertThat(todo.getTitle()).isEqualTo("new title");
		assertThat(todo.getDescription()).isEqualTo("new description");

		assertThat(response.title()).isEqualTo("new title");
		assertThat(response.description()).isEqualTo("new description");
	}

	@Test
	@DisplayName("Todo 수정_실패_존재하지 않는 Todo")
	void update_fail_todoNotFound() {
		// given
		Long memberId = 1L;
		Long todoId = 1L;
		TodoRequest request = new TodoRequest(TodoStatus.READY, "new title", "new description");

		when(todoRepository.findById(todoId))
			.thenReturn(Optional.empty());

		// when, then
		assertThatThrownBy(() -> todoService.update(memberId, todoId, request))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.TODO_NOT_FOUND);
	}

	@Test
	@DisplayName("Todo 수정_실패_작성자가 아닌 경우")
	void update_fail_notOwner() {
		// given
		Long memberId = 1L;
		Long otherMemberId = 2L;
		Long todoId = 1L;
		Member member = getMemberWithId(memberId);
		Todo todo = getTodoWithId(todoId, member);
		TodoRequest request = new TodoRequest(TodoStatus.READY, "new title", "new description");

		when(todoRepository.findById(todoId))
			.thenReturn(Optional.of(todo));

		// when, then
		assertThatThrownBy(() -> todoService.update(otherMemberId, todoId, request))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.FORBIDDEN);

		assertThat(todo.getTitle()).isEqualTo("test title");
		assertThat(todo.getDescription()).isEqualTo("test description");
	}

	@Test
	@DisplayName("Todo 삭제_성공")
	void delete_success() {
		// given
		Long memberId = 1L;
		Long todoId = 1L;
		Member member = getMemberWithId(memberId);
		Todo todo = getTodoWithId(todoId, member);

		when(todoRepository.findById(todoId))
			.thenReturn(Optional.of(todo));

		// when
		todoService.delete(memberId, todoId);

		// then
		verify(todoRepository).delete(todo);
	}

	@Test
	@DisplayName("Todo 삭제_실패_존재하지 않는 Todo")
	void delete_fail_todoNotFound() {
		// given
		Long memberId = 1L;
		Long todoId = 1L;

		when(todoRepository.findById(todoId))
			.thenReturn(Optional.empty());

		// when, then
		assertThatThrownBy(() -> todoService.delete(memberId, todoId))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.TODO_NOT_FOUND);

		verify(todoRepository, never()).delete(any(Todo.class));
	}

	@Test
	@DisplayName("Todo 삭제_실패_작성자가 아닌 경우")
	void delete_fail_notOwner() {
		// given
		Long memberId = 1L;
		Long otherMemberId = 2L;
		Long todoId = 1L;
		Member member = getMemberWithId(memberId);
		Todo todo = getTodoWithId(todoId, member);

		when(todoRepository.findById(todoId))
			.thenReturn(Optional.of(todo));

		// when, then
		assertThatThrownBy(() -> todoService.delete(otherMemberId, todoId))
			.isInstanceOf(ServiceException.class)
			.extracting(exception -> ((ServiceException) exception).getErrorCode())
			.isEqualTo(ErrorCode.FORBIDDEN);

		verify(todoRepository, never()).delete(any(Todo.class));
	}

	@Test
	@DisplayName("Todo 조회_성공_조건 미지정")
	void findAll_success_withoutCondition() {
		int defaultPageNumber = 1;
		int defaultPageLimit = 10;
		int totalElementCount = 20;

		// given
		Long memberId = 1L;
		TodoSearchRequest request = new TodoSearchRequest(null, null, null, null, null);

		when(todoRepository.searchByOwnerId(eq(memberId), isNull(), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of(), PageRequest.of(defaultPageNumber - 1, defaultPageLimit), totalElementCount));

		// when
		PageResponse<TodoResponse> response = todoService.findAll(memberId, request);

		// then
		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(todoRepository).searchByOwnerId(eq(memberId), isNull(), captor.capture());
		Sort.Order order = captor.getValue()
			.getSort()
			.getOrderFor(TodoSort.CREATED.getProperty());

		assertThat(order).isNotNull();
		assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
		assertThat(captor.getValue().getPageNumber()).isEqualTo(defaultPageNumber - 1);
		assertThat(captor.getValue().getPageSize()).isEqualTo(defaultPageLimit);

		assertThat(response.page()).isEqualTo(defaultPageNumber);
		assertThat(response.limit()).isEqualTo(defaultPageLimit);
		assertThat(response.total()).isEqualTo(2);
	}

	@Test
	@DisplayName("Todo 조회_성공_조건 지정")
	void findAll_success_withCondition() {
		// given
		Long memberId = 1L;
		int requestPage = 2;
		int requestLimit = 5;
		int totalElementCount = 20;
		TodoSearchRequest request = new TodoSearchRequest(requestPage, requestLimit, TodoStatus.READY, TodoSort.TITLE, SortDirection.DESC);

		when(todoRepository.searchByOwnerId(eq(memberId), eq(TodoStatus.READY), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of(), PageRequest.of(requestPage - 1, requestLimit), totalElementCount));

		// when
		PageResponse<TodoResponse> response = todoService.findAll(memberId, request);

		// then
		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(todoRepository).searchByOwnerId(eq(memberId), eq(TodoStatus.READY), captor.capture());
		Sort.Order order = captor.getValue()
			.getSort()
			.getOrderFor(TodoSort.TITLE.getProperty());

		assertThat(order).isNotNull();
		assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
		assertThat(captor.getValue().getPageNumber()).isEqualTo(requestPage - 1);
		assertThat(captor.getValue().getPageSize()).isEqualTo(requestLimit);

		assertThat(response.page()).isEqualTo(requestPage);
		assertThat(response.limit()).isEqualTo(requestLimit);
		assertThat(response.total()).isEqualTo(4);
	}

	private Member getMemberWithId(Long id) {
		Member member = new Member("tester", "tester@test.com", "1234");
		ReflectionTestUtils.setField(member, "id", id);

		return member;
	}

	private Todo getTodoWithId(Long id, Member member) {
		Todo todo = new Todo(member, "test title", "test description");
		ReflectionTestUtils.setField(todo, "id", id);

		return todo;
	}
}
