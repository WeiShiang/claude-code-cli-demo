package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase.CreateProductCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdateStockUseCase;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/products")
class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final QueryProductUseCase queryProductUseCase;
    private final UpdateStockUseCase updateStockUseCase;

    ProductController(CreateProductUseCase createProductUseCase,
                      QueryProductUseCase queryProductUseCase,
                      UpdateStockUseCase updateStockUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.queryProductUseCase = queryProductUseCase;
        this.updateStockUseCase = updateStockUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductIdResponse createProduct(@RequestBody @Valid CreateProductRequest request) {
        var command = new CreateProductCommand(
                request.name(),
                new Sku(request.sku()),
                new Money(request.price(), Currency.getInstance(request.currency())),
                new CategoryId(request.categoryId())
        );
        var productId = createProductUseCase.createProduct(command);
        return new ProductIdResponse(productId.value());
    }

    @GetMapping("/{productId}")
    ResponseEntity<ProductResponse> findById(@PathVariable UUID productId) {
        return queryProductUseCase.findById(new ProductId(productId))
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{productId}/stock/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reserve(@PathVariable UUID productId, @RequestBody @Valid StockAmountRequest request) {
        updateStockUseCase.reserve(new ProductId(productId), request.amount());
    }

    @PutMapping("/{productId}/stock/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void release(@PathVariable UUID productId, @RequestBody @Valid StockAmountRequest request) {
        updateStockUseCase.release(new ProductId(productId), request.amount());
    }

    @PutMapping("/{productId}/stock/deduct")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deduct(@PathVariable UUID productId, @RequestBody @Valid StockAmountRequest request) {
        updateStockUseCase.deduct(new ProductId(productId), request.amount());
    }
}
