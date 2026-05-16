package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateProductService implements CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;

    public CreateProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    public ProductId createProduct(CreateProductCommand command) {
        if (productRepository.existsBySku(command.sku())) {
            throw new DuplicateSkuException(command.sku());
        }
        if (!categoryRepository.existsById(command.categoryId())) {
            throw new CategoryNotFoundException(command.categoryId());
        }
        var product = Product.create(command.name(), command.sku(), command.price(), command.categoryId());
        productRepository.save(product);
        stockRepository.save(Stock.of(product.getId(), 0));
        return product.getId();
    }
}
