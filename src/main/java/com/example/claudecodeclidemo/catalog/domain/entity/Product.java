package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.PriceChangedEvent;
import com.example.claudecodeclidemo.catalog.domain.event.ProductArchivedEvent;
import com.example.claudecodeclidemo.catalog.domain.event.ProductPublishedEvent;
import com.example.claudecodeclidemo.catalog.domain.event.ProductUnpublishedEvent;
import com.example.claudecodeclidemo.catalog.domain.event.UnpublishReason;
import com.example.claudecodeclidemo.catalog.domain.exception.CurrencyMismatchException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductNameException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductStateTransitionException;
import com.example.claudecodeclidemo.catalog.domain.exception.MissingPriceException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductArchivedException;
import com.example.claudecodeclidemo.catalog.domain.vo.Attribute;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.ListPrice;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import com.example.claudecodeclidemo.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Product extends AggregateRoot<ProductId> {

    private final ProductId id;
    private final Sku sku;
    private String name;
    private String description;
    private ListPrice listPrice;
    private final Set<CategoryId> categoryIds = new LinkedHashSet<>();
    private final List<Attribute> attributes = new ArrayList<>();
    private final List<String> mediaUrls = new ArrayList<>();
    private ProductStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(ProductId id, Sku sku, String name, String description, Instant now) {
        this.id = Objects.requireNonNull(id, "id");
        this.sku = Objects.requireNonNull(sku, "sku");
        this.name = validateName(name);
        this.description = description == null ? "" : description;
        this.status = ProductStatus.DRAFT;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Product createDraft(Sku sku, String name, String description) {
        return new Product(ProductId.generate(), sku, name, description, Instant.now());
    }

    public static Product reconstitute(ProductId id, Sku sku, String name, String description,
                                       ListPrice listPrice, Set<CategoryId> categoryIds,
                                       List<Attribute> attributes, List<String> mediaUrls,
                                       ProductStatus status, Instant createdAt, Instant updatedAt) {
        Product p = new Product(id, sku, name, description, createdAt);
        p.listPrice = listPrice;
        if (categoryIds != null) p.categoryIds.addAll(categoryIds);
        if (attributes != null) p.attributes.addAll(attributes);
        if (mediaUrls != null) p.mediaUrls.addAll(mediaUrls);
        p.status = status;
        p.updatedAt = updatedAt;
        return p;
    }

    @Override public ProductId getId() { return id; }
    public Sku getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Optional<ListPrice> getListPrice() { return Optional.ofNullable(listPrice); }
    public Set<CategoryId> getCategoryIds() { return Set.copyOf(categoryIds); }
    public List<Attribute> getAttributes() { return List.copyOf(attributes); }
    public List<String> getMediaUrls() { return List.copyOf(mediaUrls); }
    public ProductStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void publish() {
        ensureNotArchived();
        if (status != ProductStatus.DRAFT) {
            throw new InvalidProductStateTransitionException(
                    "only DRAFT can publish, current=" + status);
        }
        if (listPrice == null) {
            throw new MissingPriceException("product must have listPrice before publish");
        }
        status = ProductStatus.PUBLISHED;
        updatedAt = Instant.now();
        registerEvent(new ProductPublishedEvent(
                UUID.randomUUID(), updatedAt, id.value(), sku.value(), name,
                listPrice.money(), categoryUuids()));
    }

    public void unpublish(UnpublishReason reason) {
        ensureNotArchived();
        if (status != ProductStatus.PUBLISHED) {
            throw new InvalidProductStateTransitionException(
                    "only PUBLISHED can unpublish, current=" + status);
        }
        Objects.requireNonNull(reason, "reason");
        status = ProductStatus.DRAFT;
        updatedAt = Instant.now();
        registerEvent(new ProductUnpublishedEvent(
                UUID.randomUUID(), updatedAt, id.value(), sku.value(), reason));
    }

    public void archive() {
        ensureNotArchived();
        if (status != ProductStatus.PUBLISHED) {
            throw new InvalidProductStateTransitionException(
                    "only PUBLISHED can archive, current=" + status);
        }
        status = ProductStatus.ARCHIVED;
        updatedAt = Instant.now();
        registerEvent(new ProductArchivedEvent(UUID.randomUUID(), updatedAt, id.value()));
    }

    public void updatePrice(Money newPrice) {
        ensureNotArchived();
        Objects.requireNonNull(newPrice, "newPrice");
        if (listPrice != null && !listPrice.money().currency().equals(newPrice.currency())) {
            throw new CurrencyMismatchException(
                    "currency mismatch: existing=" + listPrice.money().currency()
                            + ", new=" + newPrice.currency());
        }
        ListPrice nextPrice = ListPrice.of(newPrice);
        if (listPrice != null && listPrice.money().equals(newPrice)) {
            return;
        }
        Money previous = listPrice == null ? null : listPrice.money();
        listPrice = nextPrice;
        updatedAt = Instant.now();
        registerEvent(new PriceChangedEvent(
                UUID.randomUUID(), updatedAt, id.value(), sku.value(), previous, newPrice));
    }

    public void rename(String newName) {
        ensureNotArchived();
        this.name = validateName(newName);
        updatedAt = Instant.now();
    }

    public void updateDescription(String newDescription) {
        ensureNotArchived();
        this.description = newDescription == null ? "" : newDescription;
        updatedAt = Instant.now();
    }

    public void assignCategory(CategoryId categoryId) {
        ensureNotArchived();
        Objects.requireNonNull(categoryId, "categoryId");
        categoryIds.add(categoryId);
        updatedAt = Instant.now();
    }

    public void removeCategory(CategoryId categoryId) {
        ensureNotArchived();
        Objects.requireNonNull(categoryId, "categoryId");
        categoryIds.remove(categoryId);
        updatedAt = Instant.now();
    }

    public void addAttribute(Attribute attribute) {
        ensureNotArchived();
        Objects.requireNonNull(attribute, "attribute");
        attributes.add(attribute);
        updatedAt = Instant.now();
    }

    public void removeAttribute(Attribute attribute) {
        ensureNotArchived();
        attributes.remove(attribute);
        updatedAt = Instant.now();
    }

    public void addMedia(String url) {
        ensureNotArchived();
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("media url cannot be blank");
        }
        mediaUrls.add(url);
        updatedAt = Instant.now();
    }

    public void removeMedia(String url) {
        ensureNotArchived();
        mediaUrls.remove(url);
        updatedAt = Instant.now();
    }

    private void ensureNotArchived() {
        if (status == ProductStatus.ARCHIVED) {
            throw new ProductArchivedException("archived products cannot be modified");
        }
    }

    private Set<UUID> categoryUuids() {
        Set<UUID> uuids = new LinkedHashSet<>();
        for (CategoryId c : categoryIds) uuids.add(c.value());
        return uuids;
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductNameException("name cannot be blank");
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
