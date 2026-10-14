package com.example.todolistapi.todo.dto.converter;

import java.util.Locale;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.example.todolistapi.todo.dto.TodoSort;

@Component
public class TodoSortConverter implements Converter<String, TodoSort> {
	@Override
	public TodoSort convert(String source) {
		return TodoSort.valueOf(source.toUpperCase(Locale.ROOT));
	}
}