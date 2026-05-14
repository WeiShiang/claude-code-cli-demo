package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;

@RestController
@RequestMapping("/api/products")
class ProductController {

    private final CreateProductUseCase createProduct;
    private final QueryProductUseCase queryProduct;

    ProductController(CreateProductUseCase createProduct, QueryProductUseCase queryProduct) {
        this.createProduct = createProduct;
        this.queryProduct = queryProduct;
    }

    @PostMapping
    ResponseEntity<ProductResponse> create(@RequestBody CreateProductRequest request) {
        var command = new CreateProductCommand(
                request.name(),
                new Sku(request.sku()),
                new Money(request.price(), Currency.getInstance(request.currencyCode())),
                new CategoryId(request.categoryId())
        );
        var id = createProduct.createProduct(command);
        var product = queryProduct.findById(id);
        return ResponseEntity
                .created(URI.create("/api/products/" + id.value()))
                .body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> findById(@PathVariable String id) {
        var product = queryProduct.findById(new ProductId(java.util.UUID.fromString(id)));
        return ResponseEntity.ok(ProductResponse.from(product));
    }
}
