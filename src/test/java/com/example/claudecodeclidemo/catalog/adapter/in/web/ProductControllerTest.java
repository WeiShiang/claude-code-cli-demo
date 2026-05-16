package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdateStockUseCase;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.InsufficientStockException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private static final UUID PRODUCT_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_UUID = UUID.fromString("11111111-0000-0000-0000-000000000001");

    @Mock private CreateProductUseCase createProductUseCase;
    @Mock private QueryProductUseCase queryProductUseCase;
    @Mock private UpdateStockUseCase updateStockUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(createProductUseCase, queryProductUseCase, updateStockUseCase))
                .setControllerAdvice(new CatalogExceptionHandler())
                .build();
    }

    // ── POST /api/catalog/products ─────────────────────────────────────────

    @Test
    void createProduct_validRequest_returns201WithProductId() throws Exception {
        when(createProductUseCase.createProduct(any()))
                .thenReturn(new ProductId(PRODUCT_UUID));

        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Phone",
                                  "sku": "PHONE-001",
                                  "price": "999.00",
                                  "currency": "TWD",
                                  "categoryId": "%s"
                                }
                                """.formatted(CATEGORY_UUID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(PRODUCT_UUID.toString()));
    }

    @Test
    void createProduct_duplicateSku_returns409() throws Exception {
        when(createProductUseCase.createProduct(any()))
                .thenThrow(new DuplicateSkuException(new Sku("PHONE-001")));

        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Phone",
                                  "sku": "PHONE-001",
                                  "price": "999.00",
                                  "currency": "TWD",
                                  "categoryId": "%s"
                                }
                                """.formatted(CATEGORY_UUID)))
                .andExpect(status().isConflict());
    }

    // ── GET /api/catalog/products/{productId} ────────────────────────────

    @Test
    void findById_productExists_returns200() throws Exception {
        var product = Product.create("Test Phone",
                new Sku("PHONE-001"),
                new Money(new BigDecimal("999.00"), Currency.getInstance("TWD")),
                new CategoryId(CATEGORY_UUID));
        when(queryProductUseCase.findById(any())).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/catalog/products/{id}", PRODUCT_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Phone"))
                .andExpect(jsonPath("$.sku").value("PHONE-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void findById_productNotFound_returns404() throws Exception {
        when(queryProductUseCase.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/catalog/products/{id}", PRODUCT_UUID))
                .andExpect(status().isNotFound());
    }

    // ── PUT stock/reserve ────────────────────────────────────────────────

    @Test
    void reserve_validRequest_returns204() throws Exception {
        mockMvc.perform(put("/api/catalog/products/{id}/stock/reserve", PRODUCT_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 5}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void reserve_insufficientStock_returns422() throws Exception {
        doThrow(new InsufficientStockException(2, 10))
                .when(updateStockUseCase).reserve(any(), any(int.class));

        mockMvc.perform(put("/api/catalog/products/{id}/stock/reserve", PRODUCT_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void reserve_stockNotFound_returns404() throws Exception {
        doThrow(new ProductNotFoundException(new ProductId(PRODUCT_UUID)))
                .when(updateStockUseCase).reserve(any(), any(int.class));

        mockMvc.perform(put("/api/catalog/products/{id}/stock/reserve", PRODUCT_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 5}"))
                .andExpect(status().isNotFound());
    }
}
