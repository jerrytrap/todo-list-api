package com.example.todolistapi.todo.dto.converter;

import java.util.Locale;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.todolistapi.todo.dto.SortDirection;

@Component
public class SortDirectionConverter implements Converter<String, SortDirection> {
	@Override
	public SortDirection convert(String source) {
		return SortDirection.valueOf(source.toUpperCase(Locale.ROOT));
	}
}
