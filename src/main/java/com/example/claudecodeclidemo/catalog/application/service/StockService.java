package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.out.CatalogQueryPort;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.StockReservationPort;
import com.example.claudecodeclidemo.catalog.domain.entity.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StockService implements CatalogQueryPort, StockReservationPort {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public StockService(StockRepository stockRepository,
                        ProductRepository productRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Money getPrice(ProductId productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId))
                .getPrice();
    }

    @Override
    public boolean isActive(ProductId productId) {
        return productRepository.findById(productId)
                .map(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    public void reserve(ProductId productId, int quantity) {
        var stock = findStockOrThrow(productId);
        stock.reserve(quantity);
        stockRepository.save(stock);
    }

    @Override
    public void release(ProductId productId, int quantity) {
        var stock = findStockOrThrow(productId);
        stock.release(quantity);
        stockRepository.save(stock);
    }

    @Override
    public void deduct(ProductId productId, int quantity) {
        var stock = findStockOrThrow(productId);
        stock.deduct(quantity);
        stockRepository.save(stock);
        stock.getDomainEvents().forEach(eventPublisher::publishEvent);
        stock.clearDomainEvents();
    }

    private Stock findStockOrThrow(ProductId productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
