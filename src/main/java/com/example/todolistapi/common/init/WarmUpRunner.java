package com.example.todolistapi.common.init;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.todolistapi.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarmUpRunner {
	private final JwtTokenProvider jwtTokenProvider;
	private final RestClient client = RestClient.create("http://localhost:8080");

	private static final long NOT_EXIST_ID = -1L;

	@EventListener(ApplicationReadyEvent.class)
	public void warmUp() {
		long start = System.nanoTime();

		try {
			String token = jwtTokenProvider.generateToken(NOT_EXIST_ID);

			client.get().uri("/api/v1/todos")
				.header("Authorization", "Bearer " + token)
				.retrieve()
				.toBodilessEntity();
		} catch (Exception e) {
			log.warn("warm-up 실패", e);
		}

		long durationMs = (System.nanoTime() - start) / 1_000_000;
		log.info("warm-up 완료 durationMs={}", durationMs);
	}
}
