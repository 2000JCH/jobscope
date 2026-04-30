---
name: sync-docs
description: 프로젝트 문서들이 최신 상태인지, 서로 일관성이 있는지 점검한다. Claude 종료 전 실행 권장.
---

아래 순서로 실행하라:

1. 다음 파일들을 전부 읽는다
   - `CLAUDE.md`
   - `.claude/progress.md`
   - `.claude/caution.md`
   - `.claude/portfolio-issues.md`
   - `.claude/docs/convention.md`
   - `.claude/docs/architecture.md`
   - `.claude/docs/api-design.md`
   - `.claude/docs/db-design.md`
   - `.claude/docs/frontend-convention.md`
   - `.claude/docs/phase-roadmap.md`
   - `.claude/hooks/hooks.json`
   - `.claude/agents/planner.md`
   - `.claude/agents/code-reviewer.md`

2. 아래 항목을 점검한다

   **[최신화 확인]**
   - progress.md의 "현재 단계"가 실제 완료된 단위와 일치하는지
   - progress.md의 "주요 결정 사항"이 CLAUDE.md에 반영되어 있는지
   - progress.md의 "다음 할 일"이 현재 상황에 맞는지
   - caution.md의 해결된 항목(✅)이 실제 코드에 반영되어 있는지
   - caution.md에 새로 추가해야 할 주의사항이 있는지 (최근 작업 기준)
   - portfolio-issues.md에 이번 작업에서 추가할 만한 의미 있는 이슈가 있는지 (문제 해결, 보안 개선, 성능 향상 등 — 단순 기능 구현이나 기본 설정은 제외)

   **[일관성 확인]**
   - CLAUDE.md의 Git 규칙과 progress.md의 브랜치 전략이 동일한 방향인지
   - CLAUDE.md의 단위 구현 흐름(Step 0~4)과 agents/ 파일들의 역할이 충돌하지 않는지
   - convention.md의 브랜치명 규칙과 CLAUDE.md의 브랜치 전략이 일치하는지
   - hooks.json의 차단 규칙과 CLAUDE.md의 절대 규칙이 일치하는지
   - `.github/workflows/ci.yml`의 트리거 브랜치(branches)와 paths가 현재 브랜치 전략 및 프로젝트 구조와 일치하는지

   **[문제 확인]**
   - 서로 상충되는 규칙이 있는지
   - 오래된 내용(stale)이 남아있는지
   - 합의된 결정이 반영되지 않은 파일이 있는지

3. 결과를 아래 형식으로 출력한다

   문제가 없으면:
   ```
   ✅ sync-docs 완료 — 모든 문서가 최신 상태이며 일관성 있습니다.
   ```

   문제가 있으면:
   ```
   ## sync-docs 결과

   ### 🚨 즉시 수정 필요
   - {파일명}: {문제 설명} → {수정 방법}

   ### ⚠️ 확인 필요
   - {파일명}: {문제 설명} → {수정 방법}
   ```

4. 수정이 필요한 항목이 있으면 사용자에게 수정 여부를 묻고, 승인 시 직접 파일을 수정한다