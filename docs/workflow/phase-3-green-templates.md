# Phase 3：🟢 Green Domain 實作範本

> **DDD Rule**：`functional-core-imperative-shell`、`explicit-data-flow`、`error-handling`
> **範圍**：Domain 層 + Port Interface。Application Service 完整實作保留至 Phase 5。
> 主流程文件：`../agent-workflow-ddd-tdd.md`

## 實作順序

1. Value Objects（不可變，含驗證）
2. Domain Exceptions（對應 Invariant 清單）
3. Domain Entities / Aggregate Root（含狀態機、不變量守衛）
4. Domain Events（immutable record / class，欄位含 `occurredAt: Instant`）
5. Port Interfaces（`application/port/{in,out}`）

---

## 1. Domain 層純度

```java
// ✅ 只允許 java.* import
package com.example.claudecodeclidemo.order.domain.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Order { /* ... */ }
```

**禁止**：`@Entity`、`@Table`、`@Service`、`@Component` 等 framework annotation 出現在 domain 層。

---

## 2. Domain Exception 設計（`error-handling`）

不變量違反時拋出 **typed** Domain Exception，禁用 `RuntimeException`、`IllegalStateException` 等泛型。名稱必須對應 `invariants.md`。

```java
public class InsufficientStockException extends RuntimeException {
    private final ProductId productId;
    private final int requested;
    private final int available;

    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Insufficient stock for product %s: requested=%d, available=%d"
            .formatted(productId, requested, available));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public ProductId getProductId() { return productId; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
```

---

## 3. Value Object 範本（`explicit-data-flow`）

VO 不可變；「修改」必須回傳新實例，禁止 setter 或內部 mutation。

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null)
            throw new IllegalArgumentException("amount and currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidPriceException(amount);
        this.amount = amount;
        this.currency = currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new CurrencyMismatchException(this.currency, other.currency);
        return new Money(this.amount.add(other.amount), this.currency);  // 回傳新實例
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money m)) return false;
        return amount.compareTo(m.amount) == 0 && currency.equals(m.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
```

---

## GREEN Gate

```bash
./gradlew test
# 預期：所有測試 PASS（綠燈）
```

> Git checkpoint commit prefix 見主索引附錄。
