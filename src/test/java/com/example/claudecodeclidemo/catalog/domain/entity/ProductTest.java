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
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import com.example.claudecodeclidemo.shared.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final Currency USD = Currency.getInstance("USD");

    private Product newDraft() {
        return Product.createDraft(Sku.of("ABC-123"), "Test Product", "desc");
    }

    private Product published() {
        Product p = newDraft();
        p.updatePrice(Money.of(new BigDecimal("100"), TWD));
        p.publish();
        p.pullDomainEvents();
        return p;
    }

    private Product archived() {
        Product p = published();
        p.archive();
        p.pullDomainEvents();
        return p;
    }

    @Test
    void createDraft_yields_draft_status_with_no_events() {
        Product p = newDraft();
        assertThat(p.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(p.getSku()).isEqualTo(Sku.of("ABC-123"));
        assertThat(p.getName()).isEqualTo("Test Product");
        assertThat(p.hasPendingEvents()).isFalse();
    }

    @Test
    void createDraft_rejects_blank_name() {
        assertThatThrownBy(() -> Product.createDraft(Sku.of("S-1"), "", "d"))
                .isInstanceOf(InvalidProductNameException.class);
        assertThatThrownBy(() -> Product.createDraft(Sku.of("S-1"), null, "d"))
                .isInstanceOf(InvalidProductNameException.class);
    }

    // ─── CAT-INV-002: Publish without price ───
    @Test
    void publish_without_price_throws() {
        Product p = newDraft();
        assertThatThrownBy(p::publish).isInstanceOf(MissingPriceException.class);
        assertThat(p.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void publish_with_price_succeeds_and_emits_ProductPublishedEvent() {
        Product p = newDraft();
        p.updatePrice(Money.of(new BigDecimal("100"), TWD));
        p.pullDomainEvents();
        p.publish();
        assertThat(p.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
        List<DomainEvent> events = p.pullDomainEvents();
        assertThat(events).hasSize(1);
        ProductPublishedEvent evt = (ProductPublishedEvent) events.get(0);
        assertThat(evt.productId()).isEqualTo(p.getId().value());
        assertThat(evt.sku()).isEqualTo("ABC-123");
        assertThat(evt.name()).isEqualTo("Test Product");
        assertThat(evt.listPrice()).isEqualTo(Money.of(new BigDecimal("100"), TWD));
        assertThat(evt.occurredAt()).isNotNull();
        assertThat(evt.eventId()).isNotNull();
    }

    @Test
    void publish_when_already_published_throws() {
        Product p = published();
        assertThatThrownBy(p::publish).isInstanceOf(InvalidProductStateTransitionException.class);
    }

    @Test
    void unpublish_from_published_returns_to_draft_and_emits_event() {
        Product p = published();
        p.unpublish(UnpublishReason.DISCONTINUED);
        assertThat(p.getStatus()).isEqualTo(ProductStatus.DRAFT);
        List<DomainEvent> events = p.pullDomainEvents();
        assertThat(events).hasSize(1);
        ProductUnpublishedEvent evt = (ProductUnpublishedEvent) events.get(0);
        assertThat(evt.productId()).isEqualTo(p.getId().value());
        assertThat(evt.sku()).isEqualTo("ABC-123");
        assertThat(evt.reason()).isEqualTo(UnpublishReason.DISCONTINUED);
        assertThat(evt.occurredAt()).isNotNull();
    }

    @Test
    void unpublish_from_draft_throws() {
        Product p = newDraft();
        assertThatThrownBy(() -> p.unpublish(UnpublishReason.DISCONTINUED))
                .isInstanceOf(InvalidProductStateTransitionException.class);
    }

    @Test
    void archive_from_published_succeeds_and_emits_event() {
        Product p = published();
        p.archive();
        assertThat(p.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        List<DomainEvent> events = p.pullDomainEvents();
        assertThat(events).hasSize(1);
        ProductArchivedEvent evt = (ProductArchivedEvent) events.get(0);
        assertThat(evt.productId()).isEqualTo(p.getId().value());
        assertThat(evt.occurredAt()).isNotNull();
        assertThat(evt.eventId()).isNotNull();
    }

    @Test
    void archive_from_draft_throws() {
        Product p = newDraft();
        assertThatThrownBy(p::archive).isInstanceOf(InvalidProductStateTransitionException.class);
    }

    // ─── CAT-INV-004: Archived 不可改 ───
    @Test
    void archived_product_rejects_all_modifications() {
        Product p = archived();
        assertThatThrownBy(() -> p.updatePrice(Money.of(BigDecimal.TEN, TWD)))
                .isInstanceOf(ProductArchivedException.class);
        assertThatThrownBy(() -> p.rename("new")).isInstanceOf(ProductArchivedException.class);
        assertThatThrownBy(() -> p.updateDescription("x")).isInstanceOf(ProductArchivedException.class);
        assertThatThrownBy(p::archive).isInstanceOf(ProductArchivedException.class);
        assertThatThrownBy(() -> p.unpublish(UnpublishReason.DISCONTINUED))
                .isInstanceOf(ProductArchivedException.class);
    }

    // ─── CAT-INV-005: 改價必發 PriceChanged ───
    @Test
    void updatePrice_emits_PriceChangedEvent_when_value_changes() {
        Product p = newDraft();
        p.updatePrice(Money.of(new BigDecimal("100"), TWD));
        List<DomainEvent> events = p.pullDomainEvents();
        assertThat(events).hasSize(1);
        PriceChangedEvent evt = (PriceChangedEvent) events.get(0);
        assertThat(evt.productId()).isEqualTo(p.getId().value());
        assertThat(evt.sku()).isEqualTo("ABC-123");
        assertThat(evt.previousPrice()).isNull();
        assertThat(evt.newPrice()).isEqualTo(Money.of(new BigDecimal("100"), TWD));
        assertThat(evt.effectiveAt()).isNotNull();
        assertThat(p.getListPrice()).contains(ListPrice.of(Money.of(new BigDecimal("100"), TWD)));
    }

    @Test
    void updatePrice_second_change_carries_previousPrice_in_event() {
        Product p = newDraft();
        Money first = Money.of(new BigDecimal("100"), TWD);
        Money second = Money.of(new BigDecimal("200"), TWD);
        p.updatePrice(first);
        p.pullDomainEvents();
        p.updatePrice(second);
        PriceChangedEvent evt = (PriceChangedEvent) p.pullDomainEvents().get(0);
        assertThat(evt.previousPrice()).isEqualTo(first);
        assertThat(evt.newPrice()).isEqualTo(second);
    }

    @Test
    void updatePrice_to_same_value_emits_no_event() {
        Product p = newDraft();
        p.updatePrice(Money.of(new BigDecimal("100"), TWD));
        p.pullDomainEvents();
        p.updatePrice(Money.of(new BigDecimal("100"), TWD));
        assertThat(p.hasPendingEvents()).isFalse();
    }

    // ─── CAT-INV-003: 幣別一致 ───
    @Test
    void updatePrice_with_mismatched_currency_throws() {
        Product p = newDraft();
        p.updatePrice(Money.of(new BigDecimal("100"), TWD));
        assertThatThrownBy(() -> p.updatePrice(Money.of(new BigDecimal("3"), USD)))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void rename_changes_name() {
        Product p = newDraft();
        p.rename("New Name");
        assertThat(p.getName()).isEqualTo("New Name");
    }

    @Test
    void rename_rejects_blank() {
        Product p = newDraft();
        assertThatThrownBy(() -> p.rename("")).isInstanceOf(InvalidProductNameException.class);
    }

    @Test
    void assignCategory_adds_then_removeCategory_removes() {
        Product p = newDraft();
        CategoryId c = CategoryId.generate();
        p.assignCategory(c);
        assertThat(p.getCategoryIds()).contains(c);
        p.removeCategory(c);
        assertThat(p.getCategoryIds()).doesNotContain(c);
    }

    @Test
    void attributes_can_be_added_and_removed() {
        Product p = newDraft();
        Attribute a = Attribute.of("color", "navy");
        p.addAttribute(a);
        assertThat(p.getAttributes()).contains(a);
        p.removeAttribute(a);
        assertThat(p.getAttributes()).doesNotContain(a);
    }

    @Test
    void media_can_be_added_and_removed() {
        Product p = newDraft();
        p.addMedia("https://x/1.png");
        assertThat(p.getMediaUrls()).contains("https://x/1.png");
        p.removeMedia("https://x/1.png");
        assertThat(p.getMediaUrls()).doesNotContain("https://x/1.png");
    }
}
