package com.example.claudecodeclidemo.catalog.domain.exception;

import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(CategoryId categoryId) {
        super("Category not found: " + categoryId);
    }
}
