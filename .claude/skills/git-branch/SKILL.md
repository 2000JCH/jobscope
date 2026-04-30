---
name: git-branch
description: 변경된 파일과 코드를 분석해 적절한 브랜치로 이동하거나 새 브랜치를 생성
---

아래 순서로 실행하라:

1. `git status`, `git diff`, `git branch -a` 를 병렬로 실행해 변경 내용과 기존 브랜치 목록 파악
2. 변경된 파일과 코드 내용을 분석해 작업의 성격 파악
    - 브랜치 네이밍 규칙: `<type>/<작업내용>-<스코프>`
    - type: feat / fix / refactor / docs / chore / style / test
    - 작업내용: 영어 소문자, 하이픈(-)으로 구분 (예: feat/swagger-setup)
    - **백엔드(`jobscope-backend/`)와 프론트엔드(`jobscope-frontend/`) 변경이 모두 존재하면**
      반드시 사용자에게 아래와 같이 질문하고 답변을 기다린다:
      "백엔드와 프론트엔드 변경사항이 모두 있습니다. 어떤 스코프의 브랜치를 생성할까요?
       1) backend → feat/{도메인}-backend
       2) frontend → feat/{도메인}-frontend"
      → 사용자가 선택한 스코프로만 브랜치명 결정
    - 백엔드 변경만 있으면 자동으로 `feat/{도메인}-backend`
    - 프론트엔드 변경만 있으면 자동으로 `feat/{도메인}-frontend`
3. 기존 브랜치 중 현재 변경사항과 적절히 일치하는 브랜치가 있는지 확인
    - 있다면: `git checkout <브랜치명>` 으로 이동 후 `git pull --rebase origin develop` 을 실행해 develop 최신 변경을 먼저 반영하고, 이후 `git pull --rebase origin <브랜치명>` 실행 (브랜치가 리모트에 없으면 생략), "기존 브랜치 <브랜치명>으로 이동했습니다." 출력
    - 없다면: 반드시 `git checkout develop && git pull --rebase origin develop` 을 먼저 실행해 develop 최신화 후 `git checkout -b <브랜치명>` 으로 새 브랜치 생성 및 이동 후 "새로운 브랜치 <브랜치명>을 생성해서 이동했습니다." 출력
4. stash 등 별도 처리 없이 현재 변경사항은 그대로 유지
