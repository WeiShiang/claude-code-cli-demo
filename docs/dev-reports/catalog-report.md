# Catalog BC 開發報告

**日期**：2026-05-16
**Branch**：test
**開發者**：weihsiang

---

## 實作摘要

### Aggregate 清單

| Class | 類型 | 說明 |
|---|---|---|
| `Product` | Aggregate Root | 商品生命週期管理（建立、狀態轉換、改價） |
| `Stock` | Entity | 商品庫存管理（預留、釋出、扣除） |
| `Category` | Entity | 商品分類（根分類 / 子分類） |

### Value Object 清單

| Class | 說明 |
|---|---|
| `ProductId` | 商品唯一識別（UUID） |
| `CategoryId` | 分類唯一識別（UUID） |
| `Sku` | 商品庫存單位，格式 `[A-Z0-9-]{4,20}` |
| `Money` | 金額 + 幣別，金額 ≥ 0 |
| `ProductStatus` | 商品狀態列舉：`ACTIVE / INACTIVE / DISCONTINUED` |

### Use Case 清單

| Interface | 實作 | 說明 |
|---|---|---|
| `CreateProductUseCase` | `CreateProductService` | 建立商品 + 初始庫存（SKU 唯一性、分類存在性驗證） |
| `UpdateStockUseCase` | `UpdateStockService` | 預留 / 釋出 / 扣除庫存 |
| `QueryProductUseCase` | `QueryProductService` | 依 ProductId 查詢商品 |

### Domain Event 清單

| Event | 觸發時機 | 消費者（本 BC 內） |
|---|---|---|
| `ProductCreatedEvent` | `Product.create()` | — |
| `ProductPriceChangedEvent` | `Product.changePrice()` | — |
| `StockDepletedEvent` | `Stock.deduct()` 導致 `quantity == 0` | — |

### API 端點清單

| Method | Path | Use Case | 回應 |
|---|---|---|---|
| POST | `/api/catalog/products` | `CreateProductUseCase` | 201 + `productId` |
| GET | `/api/catalog/products/{productId}` | `QueryProductUseCase` | 200 / 404 |
| PUT | `/api/catalog/products/{productId}/stock/reserve` | `UpdateStockUseCase` | 204 |
| PUT | `/api/catalog/products/{productId}/stock/release` | `UpdateStockUseCase` | 204 |
| PUT | `/api/catalog/products/{productId}/stock/deduct` | `UpdateStockUseCase` | 204 |

---

## TDD 循環記錄

| Phase | 內容 | Commit |
|---|---|---|
| 🔴 RED | ProductTest / StockTest / CreateProductServiceTest（17 負向測試，全 FAIL） | `f44a632` |
| 🟢 GREEN | Domain VO / Exception / Entity / Event / Port Interface 最小實作 | `fd8bdfa` |
| 🔵 REFACTOR | 命名、CQS、錯誤處理、控制流程清理 | `93cc938` |
| Application | CreateProductService / UpdateStockService / QueryProductService | `9f42cdd` |
| Adapter | ProductController / JPA Adapter / Exception Handler | `30ffc8b` |

---

## 覆蓋率（JaCoCo Line Coverage）

| 層次 | Line Coverage | 門檻 | 結果 |
|---|---|---|---|
| domain/entity | 96.3% (79/82) | ≥ 90% | ✅ |
| domain/vo | 91.3% (21/23) | ≥ 90% | ✅ |
| domain/exception | 100% (24/24) | ≥ 90% | ✅ |
| domain/event | 100% (3/3) | ≥ 90% | ✅ |
| application/service | 100% (34/34) | ≥ 80% | ✅ |
| adapter/in/web | 87.5% (35/40) | ≥ 70% | ✅ |
| adapter/out/persistence | 92.0% (69/75) | ≥ 70% | ✅ |
| **整體** | **93.9% (275/293)** | ≥ 60% | ✅ |

---

## 驗收 Gate 結果

| # | 項目 | 結果 |
|---|---|---|
| V-1 | 所有測試通過（80 tests, 0 failures） | ✅ |
| V-2 | Line Coverage 整體 93.9%，domain ≥ 91% | ✅ |
| V-3 | Domain 層無 Spring / JPA import | ✅ |
| V-4 | 命名符合 ubiquitous-language.md | ✅ |
| V-5 | 每條 Invariant 有負向測試（共 17 個 assertThatThrownBy） | ✅ |
| V-6 | 3 個 Domain Event 均有 getDomainEvents() 驗證測試 | ✅ |

---

## DDD Rules 合規結果

| Rule | 狀態 | 備註 |
|---|---|---|
| `functional-core-imperative-shell` | ✅ | Domain 層僅 `java.*` import |
| `clean-architecture-ddd` | ✅ | 依賴方向：Adapter → Application → Domain |
| `separation-of-concerns` | ✅ | JPA annotation 只在 `adapter/out/persistence/` |
| `library-first-approach` | ✅ | 使用 `jakarta.validation`、Spring Data JPA、`@RestControllerAdvice` |

---

## 已知限制與待辦

- **Domain Event 發布**：`domainEvents` 目前僅存於 Aggregate 記憶體中，尚未整合 Spring `ApplicationEventPublisher`。若需跨 BC 通知（如 order BC 訂閱 `StockDepletedEvent`），需在 Application Service 層加入 event publishing。
- **樂觀鎖**：`StockJpaEntity` 未加 `@Version`，高並發場景可能有 lost update 問題。
- **Test Slice 限制**：Spring Boot 4.x 已移除 `@WebMvcTest` / `@DataJpaTest`，Controller 測試改用 `MockMvcBuilders.standaloneSetup()`，Persistence 測試改用 `@SpringBootTest @Transactional`。
- **分類管理 Use Case**：`CategoryRepository.save()` 有實作但無對應 Use Case，若需建立分類 API 端點需補充 `CreateCategoryUseCase`。
