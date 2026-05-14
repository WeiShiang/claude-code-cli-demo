package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class StockRepositoryAdapter implements StockRepository {

    private final StockJpaRepository jpa;

    StockRepositoryAdapter(StockJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Stock save(Stock stock) {
        var existing = jpa.findByProductId(stock.getProductId().value());
        var entity = existing.map(e -> {
            e.quantity = stock.getQuantity();
            e.reserved = stock.getReserved();
            return e;
        }).orElseGet(() -> new StockJpaEntity(
                stock.getProductId().value(),
                stock.getSku().value(),
                stock.getQuantity(),
                stock.getReserved()
        ));
        jpa.save(entity);
        return stock;
    }

    @Override
    public Optional<Stock> findByProductId(ProductId productId) {
        return jpa.findByProductId(productId.value()).map(this::toDomain);
    }

    private Stock toDomain(StockJpaEntity e) {
        return Stock.reconstitute(
                new ProductId(e.productId),
                new Sku(e.sku),
                e.quantity,
                e.reserved
        );
    }
}
