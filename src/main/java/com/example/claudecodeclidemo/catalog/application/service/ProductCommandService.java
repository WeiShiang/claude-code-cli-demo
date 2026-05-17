package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.ArchiveProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.AssignCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.PublishProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.RemoveCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.UnpublishProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdatePriceUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdateProductDetailsUseCase;
import com.example.claudecodeclidemo.catalog.application.port.out.DomainEventPublisher;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.event.UnpublishReason;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductCommandService implements
        CreateProductUseCase,
        PublishProductUseCase,
        UnpublishProductUseCase,
        ArchiveProductUseCase,
        UpdatePriceUseCase,
        UpdateProductDetailsUseCase,
        AssignCategoryUseCase,
        RemoveCategoryUseCase {

    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public ProductCommandService(ProductRepository productRepository,
                                 DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ProductId createProduct(CreateProductCommand command) {
        if (productRepository.existsBySku(command.sku())) {
            throw new DuplicateSkuException("sku already exists: " + command.sku().value());
        }
        Product product = Product.createDraft(command.sku(), command.name(), command.description());
        productRepository.save(product);
        publishPending(product);
        return product.getId();
    }

    @Override
    public void publishProduct(ProductId productId) {
        Product product = loadProduct(productId);
        product.publish();
        productRepository.save(product);
        publishPending(product);
    }

    @Override
    public void unpublishProduct(ProductId productId, UnpublishReason reason) {
        Product product = loadProduct(productId);
        product.unpublish(reason);
        productRepository.save(product);
        publishPending(product);
    }

    @Override
    public void archiveProduct(ProductId productId) {
        Product product = loadProduct(productId);
        product.archive();
        productRepository.save(product);
        publishPending(product);
    }

    @Override
    public void updatePrice(ProductId productId, Money newPrice) {
        Product product = loadProduct(productId);
        product.updatePrice(newPrice);
        productRepository.save(product);
        publishPending(product);
    }

    @Override
    public void updateDetails(UpdateProductDetailsCommand command) {
        Product product = loadProduct(command.productId());
        if (command.name() != null) {
            product.rename(command.name());
        }
        if (command.description() != null) {
            product.updateDescription(command.description());
        }
        productRepository.save(product);
        publishPending(product);
    }

    @Override
    public void assignCategory(ProductId productId, CategoryId categoryId) {
        Product product = loadProduct(productId);
        product.assignCategory(categoryId);
        productRepository.save(product);
        publishPending(product);
    }

    @Override
    public void removeCategory(ProductId productId, CategoryId categoryId) {
        Product product = loadProduct(productId);
        product.removeCategory(categoryId);
        productRepository.save(product);
        publishPending(product);
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "product not found: " + productId.value()));
    }

    private void publishPending(Product product) {
        eventPublisher.publishAll(product.pullDomainEvents());
    }
}
