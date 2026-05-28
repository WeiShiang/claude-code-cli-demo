package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.application.port.in.ArchiveProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.AssignCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.AssignCategoryUseCase.AssignCategoryCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase.CreateProductCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.PublishProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase.ProductView;
import com.example.claudecodeclidemo.catalog.application.port.in.RemoveCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.RemoveCategoryUseCase.RemoveCategoryCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.UnpublishProductUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.UnpublishProductUseCase.UnpublishProductCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdatePriceUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdatePriceUseCase.UpdatePriceCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdateProductDetailsUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdateProductDetailsUseCase.UpdateProductDetailsCommand;
import com.example.claudecodeclidemo.catalog.domain.event.UnpublishReason;
import com.example.claudecodeclidemo.catalog.domain.exception.CurrencyMismatchException;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductStateTransitionException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.Attribute;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.ListPrice;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductController.class, CatalogExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CreateProductUseCase createProduct;
    @MockitoBean PublishProductUseCase publishProduct;
    @MockitoBean UnpublishProductUseCase unpublishProduct;
    @MockitoBean ArchiveProductUseCase archiveProduct;
    @MockitoBean UpdatePriceUseCase updatePrice;
    @MockitoBean UpdateProductDetailsUseCase updateDetails;
    @MockitoBean AssignCategoryUseCase assignCategory;
    @MockitoBean RemoveCategoryUseCase removeCategory;
    @MockitoBean QueryProductUseCase queryProduct;

    private static final Currency TWD = Currency.getInstance("TWD");

    @Test
    void create_returnsIdAndForwardsCommand() throws Exception {
        UUID generated = UUID.randomUUID();
        when(createProduct.createProduct(any())).thenReturn(ProductId.of(generated));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"A-1\",\"name\":\"Phone\",\"description\":\"hi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(generated.toString()));

        ArgumentCaptor<CreateProductCommand> captor = ArgumentCaptor.forClass(CreateProductCommand.class);
        verify(createProduct).createProduct(captor.capture());
        assertThat(captor.getValue().sku()).isEqualTo(Sku.of("A-1"));
        assertThat(captor.getValue().name()).isEqualTo("Phone");
        assertThat(captor.getValue().description()).isEqualTo("hi");
    }

    @Test
    void create_duplicateSku_returns409() throws Exception {
        when(createProduct.createProduct(any()))
                .thenThrow(new DuplicateSkuException("sku exists"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"A-1\",\"name\":\"Phone\",\"description\":\"hi\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DuplicateSkuException"));
    }

    @Test
    void publish_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/products/{id}/publish", id))
                .andExpect(status().isNoContent());
        verify(publishProduct).publishProduct(ProductId.of(id));
    }

    @Test
    void publish_invalidTransition_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new InvalidProductStateTransitionException("bad state"))
                .when(publishProduct).publishProduct(any());

        mockMvc.perform(post("/api/products/{id}/publish", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("InvalidProductStateTransitionException"));
    }

    @Test
    void unpublish_returns204AndForwardsReason() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/products/{id}/unpublish", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"DISCONTINUED\"}"))
                .andExpect(status().isNoContent());
        verify(unpublishProduct).unpublishProduct(
                new UnpublishProductCommand(ProductId.of(id), UnpublishReason.DISCONTINUED));
    }

    @Test
    void archive_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/products/{id}/archive", id))
                .andExpect(status().isNoContent());
        verify(archiveProduct).archiveProduct(ProductId.of(id));
    }

    @Test
    void price_returns204AndForwardsMoney() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/products/{id}/price", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":99.50,\"currency\":\"TWD\"}"))
                .andExpect(status().isNoContent());
        verify(updatePrice).updatePrice(
                new UpdatePriceCommand(ProductId.of(id), Money.of(new BigDecimal("99.50"), TWD)));
    }

    @Test
    void price_currencyMismatch_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new CurrencyMismatchException("mismatch"))
                .when(updatePrice).updatePrice(any());

        mockMvc.perform(patch("/api/products/{id}/price", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1,\"currency\":\"USD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CurrencyMismatchException"));
    }

    @Test
    void details_returns204AndForwardsCommand() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"description\":\"d\"}"))
                .andExpect(status().isNoContent());

        ArgumentCaptor<UpdateProductDetailsCommand> captor =
                ArgumentCaptor.forClass(UpdateProductDetailsCommand.class);
        verify(updateDetails).updateDetails(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(ProductId.of(id));
        assertThat(captor.getValue().name()).isEqualTo("New");
        assertThat(captor.getValue().description()).isEqualTo("d");
    }

    @Test
    void assignCategory_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        mockMvc.perform(post("/api/products/{id}/categories", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + categoryId + "\"}"))
                .andExpect(status().isNoContent());
        verify(assignCategory).assignCategory(new AssignCategoryCommand(ProductId.of(id), CategoryId.of(categoryId)));
    }

    @Test
    void removeCategory_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        mockMvc.perform(delete("/api/products/{id}/categories/{cid}", id, categoryId))
                .andExpect(status().isNoContent());
        verify(removeCategory).removeCategory(new RemoveCategoryCommand(ProductId.of(id), CategoryId.of(categoryId)));
    }

    @Test
    void findById_returnsProductView() throws Exception {
        UUID id = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-18T10:00:00Z");
        ProductView view = new ProductView(
                ProductId.of(id), Sku.of("A-1"), "Phone", "desc",
                Optional.of(ListPrice.of(Money.of(new BigDecimal("100"), TWD))),
                Set.of(CategoryId.of(catId)),
                List.of(Attribute.of("color", "red")),
                List.of("https://img/1.png"),
                ProductStatus.PUBLISHED, now, now);
        when(queryProduct.findById(ProductId.of(id))).thenReturn(view);

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.sku").value("A-1"))
                .andExpect(jsonPath("$.listPriceAmount").value(100))
                .andExpect(jsonPath("$.listPriceCurrency").value("TWD"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.attributes[0].key").value("color"))
                .andExpect(jsonPath("$.attributes[0].value").value("red"))
                .andExpect(jsonPath("$.mediaUrls[0]").value("https://img/1.png"))
                .andExpect(jsonPath("$.categoryIds[0]").value(catId.toString()));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(queryProduct.findById(any())).thenThrow(new ProductNotFoundException("no"));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ProductNotFoundException"));
    }

    @Test
    void findBySku_returnsProductView() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-18T10:00:00Z");
        ProductView view = new ProductView(
                ProductId.of(id), Sku.of("A-1"), "Phone", "desc",
                Optional.empty(), Set.of(), List.of(), List.of(),
                ProductStatus.DRAFT, now, now);
        when(queryProduct.findBySku(eq(Sku.of("A-1")))).thenReturn(view);

        mockMvc.perform(get("/api/products").param("sku", "A-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("A-1"))
                .andExpect(jsonPath("$.listPriceAmount").doesNotExist())
                .andExpect(jsonPath("$.listPriceCurrency").doesNotExist());
    }

    @Test
    void findAll_returnsList() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-18T10:00:00Z");
        ProductView view = new ProductView(
                ProductId.of(id), Sku.of("A-1"), "Phone", "desc",
                Optional.empty(), Set.of(), List.of(), List.of(),
                ProductStatus.DRAFT, now, now);
        when(queryProduct.findAll()).thenReturn(List.of(view));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].sku").value("A-1"));
    }
}
