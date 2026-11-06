package com.example.todolistapi;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.todolistapi.security.JwtTokenProvider;
import com.example.todolistapi.todo.TodoService;
import com.example.todolistapi.todo.dto.SortDirection;
import com.example.todolistapi.todo.dto.TodoSearchRequest;
import com.example.todolistapi.todo.dto.TodoSort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarmUpRunner {
	private final StringRedisTemplate redisTemplate;
	private final JwtTokenProvider jwtTokenProvider;
	private final TodoService todoService;

	private static final long NOT_EXIST_ID = -1L;

	@EventListener(ApplicationReadyEvent.class)
	public void warmUp() {
		long start = System.currentTimeMillis();
		try {
			warmUpRedis();
			warmUpTokenProvider();
			warmUpFindTodos();

			log.info("warm-up 완료 durationMs={}", System.currentTimeMillis() - start);
		} catch (Exception e) {
			log.warn("warm-up 실패", e);
		}
	}

	private void warmUpRedis() {
		redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
	}

	private void warmUpTokenProvider() {
		String token = jwtTokenProvider.generateToken(NOT_EXIST_ID);
		jwtTokenProvider.validateToken(token);
	}

	private void warmUpFindTodos() {
		todoService.findAll(NOT_EXIST_ID, new TodoSearchRequest(1, 5, null, TodoSort.CREATED, SortDirection.DESC));
	}
}
