package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.exception.InsufficientStockException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock StockRepository stockRepository;
    @Mock ProductRepository productRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks StockService stockService;

    private static final Currency TWD = Currency.getInstance("TWD");
    private final ProductId productId = new ProductId(UUID.randomUUID());

    // ── CatalogQueryPort: getPrice ────────────────────────────────────

    @Test
    void 商品不存在時getPrice拋出ProductNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.getPrice(productId))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // ── CatalogQueryPort: isActive ────────────────────────────────────

    @Test
    void 商品不存在時isActive回傳false() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThat(stockService.isActive(productId)).isFalse();
    }

    // ── StockReservationPort: reserve ─────────────────────────────────

    @Test
    void 庫存不存在時reserve拋出ProductNotFoundException() {
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.reserve(productId, 1))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void 庫存不足時reserve拋出InsufficientStockException() {
        var stock = Stock.create(productId, 2);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> stockService.reserve(productId, 5))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void 庫存充足時reserve成功並持久化() {
        var stock = Stock.create(productId, 10);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        stockService.reserve(productId, 3);

        verify(stockRepository).save(argThat(s -> s.getReserved() == 3));
    }

    // ── StockReservationPort: release ─────────────────────────────────

    @Test
    void release成功時持久化更新後的庫存() {
        var stock = Stock.create(productId, 10);
        stock.reserve(5);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        stockService.release(productId, 3);

        verify(stockRepository).save(argThat(s -> s.getReserved() == 2));
    }

    // ── StockReservationPort: deduct ──────────────────────────────────

    @Test
    void deduct成功時庫存減少並持久化() {
        var stock = Stock.create(productId, 10);
        stock.reserve(5);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        stockService.deduct(productId, 5);

        verify(stockRepository).save(argThat(s ->
                s.getQuantity() == 5 && s.getReserved() == 0));
    }

    @Test
    void 扣除後庫存歸零時發布StockDepletedEvent() {
        var stock = Stock.create(productId, 3);
        stock.reserve(3);
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        stockService.deduct(productId, 3);

        verify(eventPublisher, atLeastOnce()).publishEvent(any());
    }
}
