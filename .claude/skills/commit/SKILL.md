---
name: commit
description: 변경사항을 분석해 커밋 메시지 자동 생성 후 커밋
---

아래 순서로 실행하라:

1. `git pull --rebase --autostash origin develop` 을 먼저 실행해 최신 상태로 동기화
    - `--autostash` 옵션으로 unstaged 변경사항을 자동으로 stash/restore 처리
    - 충돌(conflict)이 발생하면 "충돌이 발생해 진행을 중단했습니다. 직접 해결 후 다시 실행해 주세요." 라고 알리고 즉시 중단
2. 현재 브랜치명을 확인해 **스코프 자동 감지**
    - 브랜치명이 `-backend`로 끝나면 → `jobscope-backend/` 하위 파일 + 루트의 `docker-compose*.yml` 파일 커밋 대상으로 한정
      단, docker-compose 변경이 백엔드 기능과 연관이 없으면 제외
    - 브랜치명이 `-frontend`로 끝나면 → `jobscope-frontend/` 하위 파일만 커밋 대상으로 한정
    - 그 외 → 전체 변경 파일 대상 (기존 동작)
3. `git status`, `git diff`, `git log --oneline -5` 를 병렬로 실행해 변경 내용과 이 프로젝트의 커밋 메시지 스타일 파악
4. 스코프에 해당하는 변경 파일을 **기능 단위**로 묶어 커밋을 분리한다
    - 기능 단위 기준: 독립적으로 동작하는 하나의 기능 (빌드 가능, revert 가능)
    - 같은 기능을 구현하는 Entity/Repository/Service/Controller/DTO는 하나의 커밋으로 묶는다
    - 서로 다른 기능(예: 로그인 vs 프로필 수정)은 커밋을 분리한다
    - 설정 파일(`config/`, `application.yml` 등)은 관련 기능 커밋에 포함한다
5. 각 커밋 메시지 작성
    - 형식: `<type>: <내용>` (한국어)
    - type: feat / fix / refactor / docs / chore / style / test
    - 제목 50자 이내
    - 변경이 복잡한 경우 본문에 bullet point로 상세 설명 추가
6. 민감한 파일(.env, 시크릿 등)은 절대 스테이징하지 않음
7. 기능 단위로 `git add <파일명>` → `git commit` 을 반복 실행
8. `git push` 는 하지 않음 — 사용자가 명시적으로 요청할 때만 push
9. 커밋 완료 후 생성된 커밋 목록과 현재 브랜치명을 사용자에게 알려줄 것