package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService implements CreateProductUseCase, QueryProductUseCase {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductService(ProductRepository productRepository,
                          StockRepository stockRepository,
                          CategoryRepository categoryRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ProductId createProduct(CreateProductCommand command) {
        if (productRepository.findBySku(command.sku()).isPresent()) {
            throw new DuplicateSkuException(command.sku());
        }
        if (!categoryRepository.exists(command.categoryId())) {
            throw new CategoryNotFoundException(command.categoryId());
        }
        var product = Product.create(command.name(), command.sku(), command.price(), command.categoryId());
        var saved = productRepository.save(product);
        stockRepository.save(Stock.create(saved.getId(), product.getSku(), 0));
        product.getDomainEvents().forEach(eventPublisher::publishEvent);
        product.clearDomainEvents();
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(ProductId id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
