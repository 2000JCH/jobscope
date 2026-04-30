# CLAUDE.md — JobScope 프로젝트

## Communication

- 도구 사용 허가를 요청할 때(tool use confirmation)는 항상 한국어로 질문할 것

## 구현 원칙

- 하나의 단위 = entity → repository → service → controller → dto(request, response) 전부
- 비즈니스 로직은 Entity에, Service는 흐름 조율만 담당 (경량 DDD)
- 하나의 단위를 완성한 후 반드시 멈추고 사용자 확인을 기다린다
- 사용자가 명시적으로 "다음 진행해줘" 라고 할 때만 다음 단계로 넘어간다
- Global 기반(BaseEntity, ApiResponse, ErrorCode 등)은 선행 단위로 먼저 완성한다
- ErrorCode 항목 추가는 해당 도메인 단위에 포함한다

### 단위 구현 전체 흐름 (순서 엄수)

Step 0. 구현 시작 전 체크리스트
        - progress.md를 읽어 현재 진행 상태 확인
        - 아래 5개 문서를 전부 읽는다
          architecture.md / db-design.md / api-design.md / convention.md / phase-roadmap.md
        - 프론트엔드 작업 포함 시 frontend-convention.md 추가
        - .claude/agents/planner.md의 출력 형식에 따라 직접 구현 계획을 수립하고 사용자 승인을 받는다
        - Step 0 완료 전 절대 코드를 작성하지 않는다

Step 1. 코드 작성
        - 브랜치 신경 쓰지 않고 코드만 작성한다
        - Entity → Repository → Service → Controller → DTO 전체 한 번에 작성

Step 2. 테스트
        - Stop hook 자동 실행 → gradlew checkstyleMain checkstyleTest test
        - 실패 시 코드 수정 → 재실행 (통과할 때까지 반복)

Step 3. 코드 리뷰
        - code-reviewer 에이전트 자동 호출
        - 문제 발견 시 코드 수정 → 테스트 재실행 → 리뷰 재실행
        - 설계 판단이 필요한 문제는 사용자에게 질문 후 수정

Step 4. 브랜치 생성 및 커밋
        - 사용자가 /git-branch 스킬 실행 → feat/{domain}-backend 또는 feat/{domain}-frontend 브랜치 생성
        - 사용자가 /commit 스킬 실행 → develop 브랜치로 PR
        - 커밋 완료 후 "다음 진행해줘" → 다음 단계로 이동

### 모호한 요청 처리

요청이 아래 중 하나라도 해당하면 코드 작성 전에 반드시 질문한다.

- 구현 범위가 불명확한 경우
- 어떤 도메인/레이어인지 특정되지 않은 경우
- 요청이 Phase 1 범위인지 불분명한 경우

질문 후 명확한 답변을 받으면 planner 에이전트를 호출해 계획을 수립한다.

### 구현 순서

| 단위 | 내용 |
|------|------|
| 0 | Global 기반: BaseEntity, SoftDeleteEntity, ApiResponse ... JpaConfig, SwaggerConfig + build.gradle Checkstyle 플러그인 설정 + config/checkstyle/checkstyle.xml 생성 |
| 1 | 환경설정 전체 선행: 백엔드 Dockerfile + 프론트엔드 Dockerfile + nginx.conf + docker-compose.yml (MySQL 포함) + React 프로젝트 초기 설정 (폴더 구조, Vite) + Axios 인스턴스 + Zustand store 기본 구조 + .env 구조 |
| 2 | User 백엔드 (도메인 + JwtProvider, JwtFilter, SecurityConfig, KakaoAuthService) + ErrorCode USER_NOT_FOUND [✅ 완료] → User 프론트 (로그인 화면, 카카오 연동) → 연결 [✅ 완료] + 프로필 이미지 업로드·초기화 (AWS S3) [✅ 완료] |
| 3 | Application 백엔드 (도메인) + ErrorCode APPLICATION_NOT_FOUND, HISTORY_NOT_FOUND [✅ 완료] → Application 프론트 (지원 현황 CRUD, 캘린더) [✅ PR 완료] → 연결 [✅ 완료] |
| 4 | Alarm 백엔드 (도메인 + AlarmScheduler) + ErrorCode ALARM_SEND_FAILED [✅ 완료] → Alarm 프론트 (알림 이력 조회·삭제) [✅ 완료] → 연결 [✅ 완료] |

## Git 규칙

- 브랜치 전략: feat/{domain}-backend, feat/{domain}-frontend → develop → main
- 커밋은 사용자가 /git-branch → /commit 스킬 순서로 직접 진행한다
- --no-verify 옵션으로 커밋 금지

## 프로젝트 개요

취준생/이직자가 여러 회사 지원 현황을 한 곳에서 관리하는 서비스.
**Phase 1(MVP) 완료 및 운영 배포 완료 (https://jscope.duckdns.org)**
현재는 Phase 2 이상 기능도 사용자 요청 시 구현 가능하다.

Phase 3 이상 해당 키워드는 신중히 검토 후 구현: group_study, is_public, 그룹 스터디, B2B, 부트캠프

---

## 기술 스택

**백엔드**

- Java 21 / Spring Boot 3.4+ / Gradle
- Spring Security (JWT) / Spring Data JPA / Spring Scheduler
- MySQL 8.0 (Docker) / Springdoc OpenAPI (Swagger)
- jjwt 0.12.6

**프론트엔드**

- React 19 / JavaScript
- Zustand / FullCalendar / Axios / PWA

---

## 절대 규칙 (위반 금지)

```
1.  Inner Class 금지 — 모든 클래스는 별도 파일로 분리
2.  @Setter / @Data 금지 — 의미있는 메서드명 사용 (softDelete(), updateAlarmEnabled() 등)
3.  양방향 연관관계 금지 — 단방향(N→1)만 허용, @OneToMany 컬렉션 필드 선언 금지
4.  @Transactional import 경로 — org.springframework.transaction.annotation.Transactional 만 사용
    jakarta.transaction.Transactional 사용 금지 (트랜잭션 미적용됨)
5.  EnumType.ORDINAL 금지 — 반드시 @Enumerated(EnumType.STRING) 사용
6.  FetchType.EAGER 금지 — @ManyToOne, @OneToMany 모두 LAZY 기본 적용
7.  Native Query 사용 시 WHERE deleted_at IS NULL 직접 명시 필수
    (@SQLRestriction은 JPQL에만 자동 적용, Native Query에는 미적용)
8.  로그는 Service 레이어에서만 — Controller, Repository에서 로그 금지
9.  HTTP 상태코드 하드코딩 금지 — ErrorCode enum에서 자동 참조
10. DELETE도 200 반환 (204 아님) — ApiResponse 형식 유지를 위해 통일
11. Entity를 Response로 직접 반환 금지 — 반드시 DTO 변환
12. 다른 도메인 Repository 직접 참조 금지 — 반드시 해당 도메인 Service 경유
    (같은 도메인 내 Repository 직접 참조는 허용 — 예: ApplicationService → ApplicationHistoryRepository)
13. '최신 히스토리' 판별 기준은 id 최댓값 — created_at 기준 사용 금지
14. AlarmScheduler 쿼리 시 deleted_at IS NULL + alarm_enabled = TRUE 조건 반드시 포함
15. APPLICATION → APPLICATION_HISTORY 연관관계 : CascadeType.ALL + orphanRemoval = true
    단, @OneToMany 컬렉션 필드는 선언하지 않음 (규칙 3 단방향 유지)
16. 소유권 검증 메서드명은 validate{대상} 패턴 사용 — 예: validateApplicationOwner()
    Application/History 조회·수정·삭제 시 반드시 호출, 불일치 시 FORBIDDEN 반환
17. .env 파일 수정 금지 — 민감 정보는 개발자가 .env 파일로 직접 관리
18. application.yml에 @Value 설정 추가 시 src/test/resources/application.properties에도 반드시 더미값 추가
    누락 시 contextLoads() 테스트가 PlaceholderResolutionException으로 실패함 (3회 이상 반복 발생)
```

---

## 참조 문서

| 문서                  | 내용                                                |
| --------------------- | --------------------------------------------------- |
| `docs/architecture.md`        | 패키지 구조, 레이어 규칙, Lombok/트랜잭션/보안 상세 |
| `docs/db-design.md`           | DDL, 인덱스, JPA 엔티티 작성 규칙                   |
| `docs/api-design.md`          | 전체 API 명세, 요청/응답 스펙                       |
| `docs/convention.md`          | 네이밍, URL, HTTP 상태코드, 커밋 규칙               |
| `docs/phase-roadmap.md`       | Phase 1~4 기능 범위 (현재는 Phase 1만)              |
| `docs/service-overview.md`    | 서비스 설명, 타겟 유저, 기술 스택                   |
| `docs/frontend-convention.md` | 프론트엔드 컴포넌트, 상태관리, API 호출 규칙        |
| `progress.md`                 | 현재 진행 상태 및 다음 할 일                        |