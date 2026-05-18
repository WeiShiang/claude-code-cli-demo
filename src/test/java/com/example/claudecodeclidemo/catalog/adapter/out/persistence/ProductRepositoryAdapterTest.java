package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.vo.Attribute;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.ListPrice;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProductRepositoryAdapter.class)
class ProductRepositoryAdapterTest {

    private static final Currency TWD = Currency.getInstance("TWD");

    @Autowired ProductRepositoryAdapter adapter;
    @Autowired EntityManager em;

    @Test
    void save_and_findById_roundtripsAllFields() {
        Product product = createPublishedProduct("A-1");

        adapter.save(product);
        em.flush();
        em.clear();

        Optional<Product> found = adapter.findById(product.getId());
        assertThat(found).isPresent();
        Product loaded = found.get();
        assertThat(loaded.getId()).isEqualTo(product.getId());
        assertThat(loaded.getSku()).isEqualTo(Sku.of("A-1"));
        assertThat(loaded.getName()).isEqualTo("Phone");
        assertThat(loaded.getDescription()).isEqualTo("desc");
        assertThat(loaded.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
        assertThat(loaded.getListPrice()).isPresent();
        assertThat(loaded.getListPrice().get().money().amount())
                .isEqualByComparingTo("199.00");
        assertThat(loaded.getListPrice().get().money().currency()).isEqualTo(TWD);
        assertThat(loaded.getCategoryIds()).hasSize(1);
        assertThat(loaded.getAttributes())
                .containsExactly(Attribute.of("color", "red"));
        assertThat(loaded.getMediaUrls()).containsExactly("https://img/1.png");
    }

    @Test
    void save_isUpsertForSameId() {
        Product product = createDraftProduct("A-2");
        adapter.save(product);
        em.flush();

        product.updatePrice(Money.of(new BigDecimal("50"), TWD));
        adapter.save(product);
        em.flush();
        em.clear();

        Product loaded = adapter.findById(product.getId()).orElseThrow();
        assertThat(loaded.getListPrice()).isPresent();
        assertThat(loaded.getListPrice().get().money().amount()).isEqualByComparingTo("50");
        assertThat(adapter.findAll()).hasSize(1);
    }

    @Test
    void save_clearsListPrice_whenDomainPriceMissing() {
        Product priced = createPublishedProduct("A-3");
        adapter.save(priced);
        em.flush();
        em.clear();

        Product unpriced = Product.reconstitute(
                priced.getId(), priced.getSku(), priced.getName(), priced.getDescription(),
                null, Set.of(), List.of(), List.of(),
                ProductStatus.DRAFT, priced.getCreatedAt(), Instant.now());
        adapter.save(unpriced);
        em.flush();
        em.clear();

        Product loaded = adapter.findById(priced.getId()).orElseThrow();
        assertThat(loaded.getListPrice()).isEmpty();
        assertThat(loaded.getCategoryIds()).isEmpty();
        assertThat(loaded.getAttributes()).isEmpty();
        assertThat(loaded.getMediaUrls()).isEmpty();
    }

    @Test
    void findBySku_returnsProduct() {
        Product product = createDraftProduct("B-1");
        adapter.save(product);
        em.flush();
        em.clear();

        Optional<Product> found = adapter.findBySku(Sku.of("B-1"));
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(product.getId());
    }

    @Test
    void findBySku_returnsEmpty_whenMissing() {
        assertThat(adapter.findBySku(Sku.of("MISSING"))).isEmpty();
    }

    @Test
    void existsBySku_returnsTrueWhenPresent_falseOtherwise() {
        Product product = createDraftProduct("C-1");
        adapter.save(product);
        em.flush();

        assertThat(adapter.existsBySku(Sku.of("C-1"))).isTrue();
        assertThat(adapter.existsBySku(Sku.of("C-NOPE"))).isFalse();
    }

    @Test
    void findAll_returnsEveryProduct() {
        adapter.save(createDraftProduct("D-1"));
        adapter.save(createDraftProduct("D-2"));
        em.flush();
        em.clear();

        List<Product> all = adapter.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(p -> p.getSku().value())
                .containsExactlyInAnyOrder("D-1", "D-2");
    }

    @Test
    void findById_returnsEmpty_whenMissing() {
        assertThat(adapter.findById(ProductId.generate())).isEmpty();
    }

    private Product createDraftProduct(String sku) {
        return Product.createDraft(Sku.of(sku), "Phone", "desc");
    }

    private Product createPublishedProduct(String sku) {
        Instant now = Instant.now();
        return Product.reconstitute(
                ProductId.generate(),
                Sku.of(sku),
                "Phone",
                "desc",
                ListPrice.of(Money.of(new BigDecimal("199.00"), TWD)),
                Set.of(CategoryId.generate()),
                List.of(Attribute.of("color", "red")),
                List.of("https://img/1.png"),
                ProductStatus.PUBLISHED,
                now,
                now);
    }
}
