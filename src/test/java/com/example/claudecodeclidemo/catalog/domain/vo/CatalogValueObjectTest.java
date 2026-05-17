package com.example.claudecodeclidemo.catalog.domain.vo;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidPriceException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidSkuException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogValueObjectTest {

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final Currency USD = Currency.getInstance("USD");

    @Nested
    class ProductIdSpec {
        @Test
        void generate_returns_unique_id() {
            assertThat(ProductId.generate()).isNotEqualTo(ProductId.generate());
        }

        @Test
        void of_wraps_uuid() {
            UUID uuid = UUID.randomUUID();
            assertThat(ProductId.of(uuid).value()).isEqualTo(uuid);
        }

        @Test
        void equals_by_value() {
            UUID uuid = UUID.randomUUID();
            assertThat(ProductId.of(uuid)).isEqualTo(ProductId.of(uuid));
        }
    }

    @Nested
    class CategoryIdSpec {
        @Test
        void generate_returns_unique_id() {
            assertThat(CategoryId.generate()).isNotEqualTo(CategoryId.generate());
        }

        @Test
        void equals_by_value() {
            UUID uuid = UUID.randomUUID();
            assertThat(CategoryId.of(uuid)).isEqualTo(CategoryId.of(uuid));
        }
    }

    @Nested
    class SkuSpec {
        @Test
        void of_creates_sku() {
            assertThat(Sku.of("ABC-123").value()).isEqualTo("ABC-123");
        }

        @Test
        void null_or_blank_throws() {
            assertThatThrownBy(() -> Sku.of(null)).isInstanceOf(InvalidSkuException.class);
            assertThatThrownBy(() -> Sku.of("")).isInstanceOf(InvalidSkuException.class);
            assertThatThrownBy(() -> Sku.of("   ")).isInstanceOf(InvalidSkuException.class);
        }

        @Test
        void equals_by_value() {
            assertThat(Sku.of("X-1")).isEqualTo(Sku.of("X-1"));
        }
    }

    @Nested
    class MoneySpec {
        @Test
        void of_creates_money() {
            Money m = Money.of(new BigDecimal("100"), TWD);
            assertThat(m.amount()).isEqualByComparingTo("100");
            assertThat(m.currency()).isEqualTo(TWD);
        }

        @Test
        void negative_amount_throws() {
            assertThatThrownBy(() -> Money.of(new BigDecimal("-1"), TWD))
                    .isInstanceOf(InvalidPriceException.class);
        }

        @Test
        void null_amount_or_currency_throws() {
            assertThatThrownBy(() -> Money.of(null, TWD)).isInstanceOf(InvalidPriceException.class);
            assertThatThrownBy(() -> Money.of(BigDecimal.ONE, null)).isInstanceOf(InvalidPriceException.class);
        }

        @Test
        void equals_by_value_and_currency() {
            assertThat(Money.of(new BigDecimal("10"), TWD)).isEqualTo(Money.of(new BigDecimal("10.00"), TWD));
            assertThat(Money.of(BigDecimal.TEN, TWD)).isNotEqualTo(Money.of(BigDecimal.TEN, USD));
        }
    }

    @Nested
    class ListPriceSpec {
        @Test
        void of_creates_list_price() {
            ListPrice p = ListPrice.of(Money.of(BigDecimal.TEN, TWD));
            assertThat(p.money().amount()).isEqualByComparingTo("10");
        }

        @Test
        void zero_amount_throws() {
            assertThatThrownBy(() -> ListPrice.of(Money.of(BigDecimal.ZERO, TWD)))
                    .isInstanceOf(InvalidPriceException.class);
        }

        @Test
        void null_money_throws() {
            assertThatThrownBy(() -> ListPrice.of(null)).isInstanceOf(InvalidPriceException.class);
        }
    }

    @Nested
    class AttributeSpec {
        @Test
        void of_creates_attribute() {
            Attribute a = Attribute.of("color", "navy");
            assertThat(a.key()).isEqualTo("color");
            assertThat(a.value()).isEqualTo("navy");
        }

        @Test
        void null_or_blank_key_throws() {
            assertThatThrownBy(() -> Attribute.of(null, "v")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Attribute.of("", "v")).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class ProductStatusSpec {
        @Test
        void enum_has_three_states() {
            assertThat(ProductStatus.values()).containsExactlyInAnyOrder(
                    ProductStatus.DRAFT, ProductStatus.PUBLISHED, ProductStatus.ARCHIVED);
        }
    }
}
