---
name: rm-branch
description: 로컬 브랜치 정리 — main, develop, 현재 브랜치를 제외한 불필요한 브랜치를 로컬과 GitHub 모두 삭제
---

아래 순서로 실행하라:

1. `git branch` 와 `git branch -r` 을 실행해 로컬 및 remote 브랜치 목록 파악

2. 아래 브랜치는 **어떤 경우에도 절대 삭제하지 않는다**
    - `main`
    - `develop`
    - 현재 체크아웃된 브랜치 (`*` 표시)

3. 나머지 브랜치를 **재사용 가능성** 기준으로 분류한다
    - **삭제 추천** (일회성, 재사용 가능성 없음): 초기 설정, 완료된 버그픽스, 단발성 작업 브랜치
      예) `chore/init-project-setup`, `fix/some-bug-backend`, `feat/global-base-setup`
    - **유지 추천** (재사용 가능성 있음): 향후 Phase 진행이나 지속적 작업에서 다시 사용될 브랜치
      예) `docs/readme-update` (README 지속 업데이트), `feat/user-backend` (Phase 2 유저 기능 확장), `style/ui-improvement-frontend` (UI 개선 작업)

4. 분류 결과를 사용자에게 보여주고 **반드시 확인을 받은 후** 삭제를 진행한다
    - 삭제 추천 목록
    - 유지 추천 목록 (사유 포함)
    - 사용자가 조정 요청 시 반영 후 재확인

5. 확인된 삭제 대상을 로컬과 GitHub 모두 삭제한다
    - 로컬: `git branch -d <브랜치명>`
      `-d` 로 삭제 실패 시(미머지 브랜치) 사용자에게 알리고 삭제하지 않음
    - GitHub: `git push origin --delete <브랜치명>`

6. 삭제 완료 후 결과를 아래 형식으로 알릴 것
    - 삭제된 브랜치 목록
    - 남아 있는 브랜치 목록 (제외 사유 포함)