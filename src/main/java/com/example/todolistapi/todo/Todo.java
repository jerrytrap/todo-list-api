package com.example.todolistapi.todo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.todolistapi.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Todo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member owner;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TodoStatus status = TodoStatus.READY;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 500)
	private String description;

	@Builder
	public Todo(Member owner, String title, String description) {
		this.owner = owner;
		this.title = title;
		this.description = description;
	}

	public void update(TodoStatus status, String title, String description) {
		this.status = status;
		this.title = title;
		this.description = description;
	}

	public boolean isOwnedBy(Long memberId) {
		return owner.getId().equals(memberId);
	}
}
