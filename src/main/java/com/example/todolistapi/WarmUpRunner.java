package com.example.todolistapi;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarmUpRunner {
	private final StringRedisTemplate redisTemplate;

	@EventListener(ApplicationReadyEvent.class)
	public void warmUp() {
		long start = System.currentTimeMillis();
		try {
			String key = "rate:warm-up";
			redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());

			log.info("warm-up 완료 durationMs={}", System.currentTimeMillis() - start);
		} catch (Exception e) {
			log.warn("warm-up 실패", e);
		}
	}
}
