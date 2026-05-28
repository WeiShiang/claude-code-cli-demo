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
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProduct;
    private final PublishProductUseCase publishProduct;
    private final UnpublishProductUseCase unpublishProduct;
    private final ArchiveProductUseCase archiveProduct;
    private final UpdatePriceUseCase updatePrice;
    private final UpdateProductDetailsUseCase updateDetails;
    private final AssignCategoryUseCase assignCategory;
    private final RemoveCategoryUseCase removeCategory;
    private final QueryProductUseCase queryProduct;

    public ProductController(CreateProductUseCase createProduct,
                             PublishProductUseCase publishProduct,
                             UnpublishProductUseCase unpublishProduct,
                             ArchiveProductUseCase archiveProduct,
                             UpdatePriceUseCase updatePrice,
                             UpdateProductDetailsUseCase updateDetails,
                             AssignCategoryUseCase assignCategory,
                             RemoveCategoryUseCase removeCategory,
                             QueryProductUseCase queryProduct) {
        this.createProduct = createProduct;
        this.publishProduct = publishProduct;
        this.unpublishProduct = unpublishProduct;
        this.archiveProduct = archiveProduct;
        this.updatePrice = updatePrice;
        this.updateDetails = updateDetails;
        this.assignCategory = assignCategory;
        this.removeCategory = removeCategory;
        this.queryProduct = queryProduct;
    }

    @PostMapping
    public IdResponse create(@RequestBody CreateRequest req) {
        ProductId id = createProduct.createProduct(
                new CreateProductCommand(Sku.of(req.sku()), req.name(), req.description()));
        return new IdResponse(id.value());
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable UUID id) {
        publishProduct.publishProduct(ProductId.of(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublish(@PathVariable UUID id, @RequestBody UnpublishRequest req) {
        unpublishProduct.unpublishProduct(new UnpublishProductCommand(ProductId.of(id), req.reason()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        archiveProduct.archiveProduct(ProductId.of(id));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Void> price(@PathVariable UUID id, @RequestBody UpdatePriceRequest req) {
        updatePrice.updatePrice(new UpdatePriceCommand(
                ProductId.of(id), Money.of(req.amount(), Currency.getInstance(req.currency()))));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> details(@PathVariable UUID id, @RequestBody UpdateDetailsRequest req) {
        updateDetails.updateDetails(new UpdateProductDetailsCommand(
                ProductId.of(id), req.name(), req.description()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/categories")
    public ResponseEntity<Void> assignCategory(@PathVariable UUID id, @RequestBody CategoryRequest req) {
        assignCategory.assignCategory(new AssignCategoryCommand(
                ProductId.of(id), CategoryId.of(req.categoryId())));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/categories/{categoryId}")
    public ResponseEntity<Void> removeCategoryEndpoint(@PathVariable UUID id, @PathVariable UUID categoryId) {
        removeCategory.removeCategory(new RemoveCategoryCommand(
                ProductId.of(id), CategoryId.of(categoryId)));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable UUID id) {
        return toResponse(queryProduct.findById(ProductId.of(id)));
    }

    @GetMapping(params = "sku")
    public ProductResponse findBySku(@RequestParam String sku) {
        return toResponse(queryProduct.findBySku(Sku.of(sku)));
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return queryProduct.findAll().stream().map(this::toResponse).toList();
    }

    private ProductResponse toResponse(ProductView v) {
        BigDecimal amount = v.listPrice().map(lp -> lp.money().amount()).orElse(null);
        String currency = v.listPrice().map(lp -> lp.money().currency().getCurrencyCode()).orElse(null);
        return new ProductResponse(
                v.id().value(), v.sku().value(), v.name(), v.description(),
                amount, currency,
                v.categoryIds().stream().map(CategoryId::value).toList(),
                v.attributes().stream().map(a -> new AttributeDto(a.key(), a.value())).toList(),
                v.mediaUrls(),
                v.status().name(),
                v.createdAt(), v.updatedAt());
    }

    public record CreateRequest(String sku, String name, String description) {}
    public record UnpublishRequest(UnpublishReason reason) {}
    public record UpdatePriceRequest(BigDecimal amount, String currency) {}
    public record UpdateDetailsRequest(String name, String description) {}
    public record CategoryRequest(UUID categoryId) {}
    public record IdResponse(UUID id) {}
    public record AttributeDto(String key, String value) {}
    public record ProductResponse(UUID id, String sku, String name, String description,
                                  BigDecimal listPriceAmount, String listPriceCurrency,
                                  List<UUID> categoryIds, List<AttributeDto> attributes,
                                  List<String> mediaUrls, String status,
                                  java.time.Instant createdAt, java.time.Instant updatedAt) {}
}
