package com.example.todolistapi.todo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TodoRepository extends JpaRepository<Todo, Long> {
	@Query("""
    SELECT t
    FROM Todo t
    WHERE t.owner.id = :memberId
      AND (:status IS NULL OR t.status = :status)
    """)
	Page<Todo> findByOwnerId(Long memberId, TodoStatus status, Pageable pageable);
}
