package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class StockRepositoryAdapter implements StockRepository {

    private final StockJpaRepository jpaRepository;

    StockRepositoryAdapter(StockJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Stock save(Stock stock) {
        jpaRepository.save(toJpa(stock));
        return stock;
    }

    @Override
    public Optional<Stock> findByProductId(ProductId productId) {
        return jpaRepository.findById(productId.value()).map(this::toDomain);
    }

    private StockJpaEntity toJpa(Stock s) {
        return new StockJpaEntity(s.getProductId().value(), s.getQuantity(), s.getReserved());
    }

    private Stock toDomain(StockJpaEntity e) {
        return Stock.reconstitute(new ProductId(e.getProductId()), e.getQuantity(), e.getReserved());
    }
}
