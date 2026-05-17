package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Category;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Category category) {
        CategoryJpaEntity entity = jpaRepository.findById(category.getId().value())
                .orElseGet(() -> new CategoryJpaEntity(
                        category.getId().value(), category.getName(),
                        category.getParentId().map(CategoryId::value).orElse(null),
                        category.getCreatedAt()));
        entity.setName(category.getName());
        entity.setParentId(category.getParentId().map(CategoryId::value).orElse(null));
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Category toDomain(CategoryJpaEntity e) {
        CategoryId parent = e.getParentId() == null ? null : CategoryId.of(e.getParentId());
        return Category.of(CategoryId.of(e.getId()), e.getName(), parent);
    }
}
