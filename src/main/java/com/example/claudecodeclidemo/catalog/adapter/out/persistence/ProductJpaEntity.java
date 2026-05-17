package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "catalog_products")
public class ProductJpaEntity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(precision = 19, scale = 4)
    private BigDecimal listPriceAmount;

    @Column(length = 3)
    private String listPriceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "catalog_product_categories",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "category_id")
    private Set<UUID> categoryIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "catalog_product_attributes",
            joinColumns = @JoinColumn(name = "product_id"))
    private List<AttributeEmbeddable> attributes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "catalog_product_media",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "url")
    private List<String> mediaUrls = new ArrayList<>();

    protected ProductJpaEntity() {}

    public ProductJpaEntity(UUID id, String sku, String name, String description,
                            BigDecimal listPriceAmount, String listPriceCurrency,
                            ProductStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.listPriceAmount = listPriceAmount;
        this.listPriceCurrency = listPriceCurrency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getListPriceAmount() { return listPriceAmount; }
    public String getListPriceCurrency() { return listPriceCurrency; }
    public ProductStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<UUID> getCategoryIds() { return categoryIds; }
    public List<AttributeEmbeddable> getAttributes() { return attributes; }
    public List<String> getMediaUrls() { return mediaUrls; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setListPriceAmount(BigDecimal listPriceAmount) { this.listPriceAmount = listPriceAmount; }
    public void setListPriceCurrency(String listPriceCurrency) { this.listPriceCurrency = listPriceCurrency; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setCategoryIds(Set<UUID> categoryIds) { this.categoryIds = categoryIds; }
    public void setAttributes(List<AttributeEmbeddable> attributes) { this.attributes = attributes; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    @Embeddable
    public static class AttributeEmbeddable {
        @Column(name = "attr_key")
        private String key;
        @Column(name = "attr_value")
        private String value;

        protected AttributeEmbeddable() {}
        public AttributeEmbeddable(String key, String value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() { return key; }
        public String getValue() { return value; }
    }
}
