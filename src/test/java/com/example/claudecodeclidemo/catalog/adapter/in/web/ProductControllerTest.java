package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.entity.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProductControllerTest {

    @Autowired WebApplicationContext context;

    @MockitoBean CreateProductUseCase createProductUseCase;
    @MockitoBean QueryProductUseCase queryProductUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Test
    void 建立商品成功回傳201() throws Exception {
        var productId = ProductId.generate();
        var product = Product.reconstitute(
                productId, "測試商品", new Sku("PROD-001"),
                new Money(new BigDecimal("100"), TWD),
                new CategoryId(CATEGORY_ID), ProductStatus.ACTIVE
        );
        when(createProductUseCase.createProduct(any())).thenReturn(productId);
        when(queryProductUseCase.findById(productId)).thenReturn(product);

        var body = objectMapper.writeValueAsString(Map.of(
                "name", "測試商品",
                "sku", "PROD-001",
                "price", 100,
                "currencyCode", "TWD",
                "categoryId", CATEGORY_ID.toString()
        ));

        mockMvc().perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("PROD-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void SKU重複時回傳409() throws Exception {
        when(createProductUseCase.createProduct(any()))
                .thenThrow(new DuplicateSkuException(new Sku("PROD-001")));

        var body = objectMapper.writeValueAsString(Map.of(
                "name", "測試商品",
                "sku", "PROD-001",
                "price", 100,
                "currencyCode", "TWD",
                "categoryId", CATEGORY_ID.toString()
        ));

        mockMvc().perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void 查詢存在商品回傳200() throws Exception {
        var productId = ProductId.generate();
        var product = Product.reconstitute(
                productId, "測試商品", new Sku("PROD-001"),
                new Money(new BigDecimal("100"), TWD),
                new CategoryId(CATEGORY_ID), ProductStatus.ACTIVE
        );
        when(queryProductUseCase.findById(any())).thenReturn(product);

        mockMvc().perform(get("/api/products/" + productId.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("測試商品"));
    }

    @Test
    void 查詢不存在商品回傳404() throws Exception {
        when(queryProductUseCase.findById(any()))
                .thenThrow(new ProductNotFoundException(ProductId.generate()));

        mockMvc().perform(get("/api/products/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
