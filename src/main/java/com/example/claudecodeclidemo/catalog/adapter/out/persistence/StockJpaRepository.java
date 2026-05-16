package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface StockJpaRepository extends JpaRepository<StockJpaEntity, UUID> {}
