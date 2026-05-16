package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {}
