package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.UpdateStockUseCase;
import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateStockService implements UpdateStockUseCase {

    private final StockRepository stockRepository;

    public UpdateStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void reserve(ProductId productId, int amount) {
        Stock stock = loadStock(productId);
        stock.reserve(amount);
        stockRepository.save(stock);
    }

    @Override
    public void release(ProductId productId, int amount) {
        Stock stock = loadStock(productId);
        stock.release(amount);
        stockRepository.save(stock);
    }

    @Override
    public void deduct(ProductId productId, int amount) {
        Stock stock = loadStock(productId);
        stock.deduct(amount);
        stockRepository.save(stock);
    }

    private Stock loadStock(ProductId productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
