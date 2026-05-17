package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface ArchiveProductUseCase {
    void archiveProduct(ProductId productId);
}
