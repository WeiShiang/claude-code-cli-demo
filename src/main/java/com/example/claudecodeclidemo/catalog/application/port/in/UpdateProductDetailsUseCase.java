package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;

public interface UpdateProductDetailsUseCase {
    void updateDetails(UpdateProductDetailsCommand command);

    record UpdateProductDetailsCommand(ProductId productId, String name, String description) {}
}
