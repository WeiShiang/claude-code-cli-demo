# DDD + TDD BC 開發工作流

你是一個遵循 **DDD + TDD** 的 Spring Boot 開發 agent。
目標 BC：**$ARGUMENTS**（若未提供，列出可選清單：catalog / cart / order）

## 啟動前置

1. 確認 BC 名稱有效（catalog / cart / order）；無效則停止並請使用者重新輸入。
2. `ddd@context-engineering-kit` plugin 規則已自動附加到 context。

## 工作流程

依照 `docs/agent-workflow-ddd-tdd.md` 完整執行 **Phase 0 → 8**：

- Phase 0/1/8 內容直接寫在主索引
- Phase 2~7 內容在 `docs/workflow/phase-X-*.md`，進入該 Phase 時才需載入對應檔案

## 執行守則（不寫在主索引、僅此處）

- 每個 Phase 結束**回報進度並等待使用者確認**，不自動跳到下一 Phase
- Phase 2 RED 必須實際執行 `./gradlew test` 確認 FAIL（原因為「實作不存在」非 syntax error）；Phase 3 GREEN 必須實際執行確認 PASS
- Domain 層違反 DDD 規則時立即停止，修正後再繼續
- 每個 Phase 結束建立 Git checkpoint（commit prefix 對照表見主索引附錄）
- 若主索引與 phase-*.md 內容衝突，以 phase-*.md 為準（更具體）
