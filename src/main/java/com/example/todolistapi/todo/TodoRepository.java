package com.example.todolistapi.todo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
	Page<Todo> findByOwnerId(Long memberId, Pageable pageable);
}
