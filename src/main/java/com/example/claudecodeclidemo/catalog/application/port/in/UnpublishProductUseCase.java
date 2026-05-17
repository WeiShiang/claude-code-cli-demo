package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.event.UnpublishReason;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface UnpublishProductUseCase {
    void unpublishProduct(ProductId productId, UnpublishReason reason);
}
