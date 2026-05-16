package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.exception.InsufficientStockException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidReleaseAmountException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStockServiceTest {

    private static final ProductId PRODUCT_ID = new ProductId(UUID.randomUUID());

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private UpdateStockService updateStockService;

    // ── reserve ──────────────────────────────────────────────────────────

    @Test
    void reserve_stockExists_callsDomainAndSaves() {
        Stock stock = Stock.of(PRODUCT_ID, 10);
        when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

        updateStockService.reserve(PRODUCT_ID, 3);

        verify(stockRepository).save(stock);
    }

    @Test
    void reserve_stockNotFound_throwsProductNotFoundException() {
        when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateStockService.reserve(PRODUCT_ID, 1))
                .isInstanceOf(ProductNotFoundException.class);

        verify(stockRepository, never()).save(any());
    }

    @Test
    void reserve_insufficientStock_throwsInsufficientStockException() {
        Stock stock = Stock.of(PRODUCT_ID, 2);
        when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> updateStockService.reserve(PRODUCT_ID, 5))
                .isInstanceOf(InsufficientStockException.class);

        verify(stockRepository, never()).save(any());
    }

    // ── release ──────────────────────────────────────────────────────────

    @Test
    void release_stockExists_callsDomainAndSaves() {
        Stock stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(5);
        when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

        updateStockService.release(PRODUCT_ID, 3);

        verify(stockRepository).save(stock);
    }

    @Test
    void release_stockNotFound_throwsProductNotFoundException() {
        when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateStockService.release(PRODUCT_ID, 1))
                .isInstanceOf(ProductNotFoundException.class);

        verify(stockRepository, never()).save(any());
    }

    @Test
    void release_amountExceedsReserved_throwsInvalidReleaseAmountException() {
        Stock stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(2);
        when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> updateStockService.release(PRODUCT_ID, 5))
                .isInstanceOf(InvalidReleaseAmountException.class);

        verify(stockRepository, never()).save(any());
    }

    // ── deduct ───────────────────────────────────────────────────────────

    @Test
    void deduct_stockExists_callsDomainAndSaves() {
        Stock stock = Stock.of(PRODUCT_ID, 10);
        stock.reserve(5);
        when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

        updateStockService.deduct(PRODUCT_ID, 5);

        verify(stockRepository).save(stock);
    }

    @Test
    void deduct_stockNotFound_throwsProductNotFoundException() {
        when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateStockService.deduct(PRODUCT_ID, 1))
                .isInstanceOf(ProductNotFoundException.class);

        verify(stockRepository, never()).save(any());
    }
}
