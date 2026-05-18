# Catalog BC 開發報告

**日期**：2026-05-18
**Branch**：`test`
**最終 commit**：`c60892f` (Phase 6 補完 + adapter bug fix)

## 實作摘要

### Aggregate

| Class | 類型 | 說明 |
|---|---|---|
| `Product` | Aggregate Root | 商品的身份、文案、媒體、分類、標價、生命週期。狀態機 `DRAFT → PUBLISHED → ARCHIVED`。發布所有 Domain Event。 |
| `Category` | Aggregate Root | 商品分類，含可選父分類。 |

### Value Objects

| Class | 說明 |
|---|---|
| `ProductId` / `CategoryId` | UUID 包裝。 |
| `Sku` | 非空字串；違反丟 `InvalidSkuException`。 |
| `Money` | `BigDecimal amount + Currency`；負金額丟 `InvalidPriceException`。 |
| `ListPrice` | 包 `Money`，必須大於 0。 |
| `Attribute` | `key/value` 字串對。 |
| `ProductStatus` | `DRAFT / PUBLISHED / ARCHIVED` 列舉。 |

### Use Cases

| Interface | 實作 | 說明 |
|---|---|---|
| `CreateProductUseCase` | `ProductCommandService` | 建立 DRAFT 商品，檢查 SKU 唯一性（CAT-INV-001）。 |
| `PublishProductUseCase` | `ProductCommandService` | `DRAFT → PUBLISHED`，需有 ListPrice（CAT-INV-002）。 |
| `UnpublishProductUseCase` | `ProductCommandService` | `PUBLISHED → DRAFT`，附 `UnpublishReason`。 |
| `ArchiveProductUseCase` | `ProductCommandService` | `PUBLISHED → ARCHIVED`，不可逆。 |
| `UpdatePriceUseCase` | `ProductCommandService` | 改 ListPrice，幣別需一致（CAT-INV-003），發 `PriceChangedEvent`（CAT-INV-005）。 |
| `UpdateProductDetailsUseCase` | `ProductCommandService` | 改 name/description。Archived 不可改（CAT-INV-004）。 |
| `AssignCategoryUseCase` / `RemoveCategoryUseCase` | `ProductCommandService` | 商品 ↔ 分類關聯。 |
| `QueryProductUseCase` | `QueryProductService` | 查 by id / sku / all（返回 `ProductView` DTO）。 |
| `CreateCategoryUseCase` / `QueryCategoryUseCase` | `CategoryService` | Category CRUD。 |

### Domain Events

| Event | 觸發時機 | 消費者 |
|---|---|---|
| `ProductPublishedEvent` | `Product.publish()` | Cart 等待 Catalog 上架通知 |
| `ProductUnpublishedEvent` | `Product.unpublish(reason)` | Cart revalidate ProductRef |
| `ProductArchivedEvent` | `Product.archive()` | Cart 強制移除已下架商品 |
| `PriceChangedEvent` | `Product.updatePrice()` 且金額變動 | Cart 更新 `currentPrice` |

事件透過 `DomainEventPublisher` port 由 `SpringDomainEventPublisher` 轉發到 `ApplicationEventPublisher`。

### Adapters

- **In (Web)**：`ProductController`、`CategoryController`、`CatalogExceptionHandler`（`@RestControllerAdvice` 將 domain exception 對映 4xx）。
- **Out (Persistence)**：`ProductJpaEntity`/`CategoryJpaEntity` 與 domain 完全分離；`ProductRepositoryAdapter`/`CategoryRepositoryAdapter` 實作 port，使用 `Product.reconstitute(...)` 重建 aggregate。
- **Out (Event)**：`SpringDomainEventPublisher` 包 `ApplicationEventPublisher`。

## TDD 循環記錄

| 階段 | Commit | 說明 |
|---|---|---|
| Phase 2 RED + Phase 3 GREEN | `d0cdbda` | 重建 catalog domain 層（VO、Entity、Invariants、Events 測試先寫，再實作通過） |
| Phase 4 REFACTOR | `5b76a4d` | Domain 層重構（命名、控制流、CQS） |
| Phase 5 Application | `bcd0a7c` | 實作 Application Services + Mock-port 單元測試 |
| Phase 6 Adapter (impl) | `e1fee26` | REST controller、JPA entity/repository、Spring event publisher（**當時無測試**） |
| Phase 6 Adapter (tests) | `c60892f` | 補上 36 個 adapter 測試 + 修一個 production bug |

## 覆蓋率（jacoco）

| 層次 | Line Covered / Missed | % |
|---|---|---|
| **整體** | 507 / 10 | **98.07%** |
| catalog/domain/entity | 144 / 6 | 96% |
| catalog/domain/event | 8 / 0 | 100% |
| catalog/domain/vo | 39 / 1 | 97.5% |
| catalog/domain/exception | 11 / 0 | 100% |
| catalog/application/service | 81 / 0 | 100% |
| catalog/application/port/in | 4 / 0 | 100% |
| catalog/application/port/out | 4 / 0 | 100% |
| catalog/adapter/in/web | 73 / 0 | 100% |
| catalog/adapter/out/persistence | 121 / 1 | 99.18% |
| catalog/adapter/out/event | 5 / 0 | 100% |

| 測試套件 | 數量 |
|---|---|
| Domain (entity/vo) | 33 |
| Application Service | 18 |
| Adapter In (Web) | 21 |
| Adapter Out (Persistence) | 13 |
| Adapter Out (Event) | 2 |
| Application Context Load | 1 |
| **Phase 6 新增** | **36** |
| **總計** | **96** |

## DDD Rules 合規結果

| Rule | 狀態 | 備註 |
|---|---|---|
| `functional-core-imperative-shell` | ✅ | `catalog/domain` 0 個 Spring/JPA/Hibernate import |
| `clean-architecture-ddd` | ✅ | Port-Adapter 分離；Adapter 只依賴 port interface |
| `separation-of-concerns` | ✅ | Controller 只做 DTO↔Command；Service 協調；Domain 含商業規則 |
| `library-first-approach` | ✅ | `@RestControllerAdvice`、JPA `@ElementCollection`、Spring `ApplicationEventPublisher` 取代手刻 |
| **V-4 Ubiquitous Language** | ✅ | Product/ProductId/Draft/Published/Archived/Category/Attribute/ListPrice/Publish/Archive/UpdatePrice/Sku 命名與 `ubiquitous-language.md` 一致 |
| **V-5 Invariant Coverage** | ✅ | CAT-INV-001 ~ 005 均有負向測試（DuplicateSku/MissingPrice/CurrencyMismatch/ProductArchived） |
| **V-6 Domain Event Coverage** | ✅ | 4 個 event 在 `ProductTest` 中均有 `registerEvent` 驗證；`SpringDomainEventPublisherTest` 驗證轉發 |

## 已知限制與待辦

- **JPA mediaUrls/attributes 順序**：以 `@ElementCollection` + `List` 儲存，未顯式 `@OrderColumn`，跨資料庫的順序保證取決於底層實作（目前 H2 觀察為插入序）。如未來上線需嚴格順序，加 `@OrderColumn`。
- **CategoryNotFound 對映**：Application service 取消失敗時對 unknown category 是否要 fail-fast 尚未在 `assignCategory` 流程覆蓋（目前 service 直接吃 `ProductRepository.findById`，不檢查 `CategoryRepository.existsById`）。若 Cart 開始依賴此完整性需補。
- **`AggregateRoot` shared 抽象** 已抽到 `shared.domain`（commit `e425e23`），未來 Cart/Order BC 重建時可直接複用 `registerEvent` / `domainEvents()` 介面。
- **Outbox 模式（CAT-INV-005 註記）**：`invariants.md` 提到「事件與寫入在同一交易」需用 outbox；目前以 `@Transactional` + `ApplicationEventPublisher` 提供事務內發布，尚未有獨立 outbox 表。對 in-process 監聽足夠，但若要跨服務可靠投遞需擴充。
- **Spring Boot 4.0 測試切片**：升級時 `WebMvcTest` 與 `DataJpaTest` 已搬到 `spring-boot-webmvc-test` / `spring-boot-data-jpa-test`，需在其他 BC 開發前同步更新 `build.gradle`（本次已加）。
