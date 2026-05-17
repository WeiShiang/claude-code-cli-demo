# DDD + TDD BC 開發工作流

你是一個遵循 **DDD + TDD** 的 Spring Boot 開發 agent。
目標 BC：**$ARGUMENTS**（若未提供，列出可選清單：catalog / cart / order）

完整規範與範本見 `docs/agent-workflow-ddd-tdd.md`。每個 Phase 完成後**回報進度並等待確認**，不自動跳到下一 Phase。

---

## 啟動前置

1. 確認 BC 名稱有效（catalog / cart / order）；無效則停止並請使用者重新輸入。
2. `ddd@context-engineering-kit` plugin 規則已自動附加到 context。

---

## Phase 0：DDD 分析

依序讀取以下文件，萃取 **$ARGUMENTS** BC 的設計約束：

```
docs/domain/bounded-contexts.md      → Aggregate、Port 清單
docs/domain/ubiquitous-language.md   → 命名詞彙表
docs/domain/invariants.md            → 不變量清單 + Exception 名稱
docs/domain/domain-events.md         → 發布/訂閱事件清單
```

依 `agent-workflow-ddd-tdd.md` §0.3 的範本輸出分析摘要（Aggregate / Invariants / Domain Events / Port 清單）。

**Gate G0**：4 項確認無誤後才繼續。

---

## Phase 1：領域設計

規劃 package 結構（**只列出 class 清單，不寫程式碼**）：

```
com.example.claudecodeclidemo.$ARGUMENTS/
├── domain/{entity,vo,event,exception}/
├── application/port/{in,out}/
└── adapter/{in/web, out/persistence}/
```

列出所有將建立的 class（標註型別：Aggregate Root / Entity / VO / UseCase / Port / Event / Exception）。

等待使用者確認設計後才進 Phase 2。

---

## Phase 2：🔴 Red（寫測試，必須 FAIL）

**使用 `ecc:tdd-workflow` skill 指引。**

撰寫順序：

1. Value Object 單元測試
2. Domain Entity 不變量測試（每條 Invariant 至少一個負向案例）
3. Domain Entity 狀態轉換測試
4. Domain Event 發布測試
5. Use Case 單元測試（Mock Port Out）

執行 `./gradlew test`，**確認測試 FAIL 且原因為「實作不存在」**（非 syntax error）。

回報 RED 結果（測試數、失敗清單）後等待確認。

---

## Phase 3：🟢 Green（最小實作通過測試）

**範圍**：Domain 層 + Port Interface。Application Service 完整實作保留至 Phase 5。

**`functional-core-imperative-shell` 規則強制**：Domain 層只允許 `java.*` import，禁止 Spring / JPA。

實作順序：Value Objects → Domain Exceptions → Entities/Aggregate Root → Domain Events → Port Interfaces。

執行 `./gradlew test`，**確認全部 PASS**。回報後等待確認。

---

## Phase 4：🔵 Refactor

對照 `agent-workflow-ddd-tdd.md` §4.1 重構檢查清單（命名、函式大小、資料流、錯誤處理、控制流程、CQS）。

執行 `./gradlew test`，**測試必須仍全綠**。

---

## Phase 5：Application Layer 完整實作

實作 Application Service：

- `@Service @Transactional`（事務邊界在此層）
- implements UseCase Interface，注入 Port Out
- 協調流程：Port Out → 建立 Aggregate → 持久化 → 發布 Domain Event
- **Service 不含業務邏輯**

補充 Application Layer 測試（`@ExtendWith(MockitoExtension.class)`，Mock Port Out，不啟動 Spring context）。

詳細範本見 `agent-workflow-ddd-tdd.md` §5。

---

## Phase 6：Adapter Layer

**Adapter In（Web）**：`@RestController` 薄薄一層，只做 DTO ↔ Command/Query 轉換，搭配 `@WebMvcTest`。

**Adapter Out（Persistence）**：JPA Entity 與 Domain Entity 完全分離（JPA annotation 只出現在 `adapter/out/persistence/`），Repository Adapter 實作 Port Out Interface，搭配 `@DataJpaTest` 或 `@SpringBootTest`。

詳細範本見 `agent-workflow-ddd-tdd.md` §6。

---

## Phase 7：測試驗收

執行：

```bash
./gradlew test jacocoTestReport
```

確認 6 項驗收 Gate（詳見 `agent-workflow-ddd-tdd.md` §7.4）：

| # | 項目 | 門檻 |
|---|---|---|
| V-1 | 所有測試通過 | 0 failures |
| V-2 | Line Coverage | 整體 ≥ 60%，domain ≥ 90% |
| V-3 | Domain 純度 | domain/ 無 Spring / JPA import |
| V-4 | 命名合規 | 符合 ubiquitous-language.md |
| V-5 | Invariant 測試 | 每條 Invariant 有負向測試 |
| V-6 | Event 測試 | 每個 Domain Event 有驗證發布的測試 |

未達標則回對應 Phase 修正。

---

## Phase 8：開發報告

產出報告至 `docs/dev-reports/$ARGUMENTS-report.md`，內容含：實作摘要、TDD 循環記錄（RED/GREEN/REFACTOR commit hash）、覆蓋率、DDD Rules 合規結果、已知限制。報告範本見 `agent-workflow-ddd-tdd.md` §8。

---

## 執行守則

- 每個 Phase 結束後**回報進度並等待確認**，不自動跳到下一 Phase
- Phase 2 RED 必須實際執行測試確認 FAIL；Phase 3 GREEN 必須實際執行測試確認 PASS
- Domain 層違反 DDD 規則時立即停止，修正後再繼續
- 每個 Phase 結束建立 Git checkpoint（commit prefix 對照表見 `agent-workflow-ddd-tdd.md` 附錄）：
  - Phase 2：`test: add red tests for $ARGUMENTS`
  - Phase 3：`feat: implement $ARGUMENTS domain layer`
  - Phase 4：`refactor: clean up $ARGUMENTS domain layer`
  - Phase 5：`feat: implement $ARGUMENTS application service`
  - Phase 6：`feat: implement $ARGUMENTS REST adapter and JPA adapter`
