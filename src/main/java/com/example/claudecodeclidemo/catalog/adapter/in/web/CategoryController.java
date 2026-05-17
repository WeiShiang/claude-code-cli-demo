package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryCategoryUseCase.CategoryView;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategory;
    private final QueryCategoryUseCase queryCategory;

    public CategoryController(CreateCategoryUseCase createCategory, QueryCategoryUseCase queryCategory) {
        this.createCategory = createCategory;
        this.queryCategory = queryCategory;
    }

    @PostMapping
    public IdResponse create(@RequestBody CreateRequest req) {
        CategoryId id = createCategory.createCategory(req.name());
        return new IdResponse(id.value());
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable UUID id) {
        return toResponse(queryCategory.findById(CategoryId.of(id)));
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return queryCategory.findAll().stream().map(this::toResponse).toList();
    }

    private CategoryResponse toResponse(CategoryView v) {
        return new CategoryResponse(
                v.id().value(),
                v.name(),
                v.parentId().map(CategoryId::value).orElse(null));
    }

    public record CreateRequest(String name) {}
    public record IdResponse(UUID id) {}
    public record CategoryResponse(UUID id, String name, UUID parentId) {}
}
