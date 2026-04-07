# JobScope

취준생/이직자가 여러 회사 지원 현황을 한 곳에서 관리하는 서비스

---

## 서비스 소개

노션/엑셀로 직접 만들어 쓰던 지원 관리를 취준에 특화된 형태로 제공합니다.
어떤 회사가 어느 단계인지, 다음 면접이 언제인지, 합격·불합격 패턴이 어떤지를 한눈에 확인할 수 있습니다.

### 핵심 기능 (Phase 1 MVP)

- 지원 현황 CRUD — 회사명, 직군, 지원 날짜, 마감일, 공고 URL, 메모 등록
- 전형 단계별 히스토리 — 서류 → 코테 → 1차 면접 → 최종 단계별 결과 기록
- 대시보드 — 전체 현황 카운트, 이번 주 면접 일정, 마감 임박 공고 (D-3 이내)
- 캘린더 — 면접 일정·서류 마감일 시각화 (FullCalendar)
- 카카오 소셜 로그인
- 카카오 알림톡 — D-7, D-3, D-1, D-Day 자동 발송 (매일 오전 9시 KST)

---

## 기술 스택

### 백엔드
| 항목 | 내용 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.4+ |
| Auth | Spring Security + JWT |
| ORM | Spring Data JPA |
| DB | MySQL 8.0 |
| Docs | Springdoc OpenAPI (Swagger) |
| Scheduler | Spring Scheduler |
| Build | Gradle |

### 프론트엔드
| 항목 | 내용 |
|------|------|
| Library | React 19 |
| Language | JavaScript |
| 상태관리 | Zustand |
| 캘린더 | FullCalendar |
| HTTP | Axios |
| 기타 | PWA (manifest + Service Worker) |

### 인프라
| 항목 | 내용 |
|------|------|
| 서버 | AWS EC2 |
| DB | MySQL 8.0 (Docker) |
| 프록시 | Nginx |
| 컨테이너 | Docker / Docker Compose |
| CI/CD | GitHub Actions |
| 프론트 배포 | Vercel / Netlify |

---

## 프로젝트 구조

```
jobscope/
├── jobscope-backend/        # Spring Boot 백엔드
│   └── src/main/java/com/jobscope/
│       ├── domain/
│       │   ├── user/        # 유저 도메인
│       │   ├── application/ # 지원 현황 도메인
│       │   └── alarm/       # 알림 도메인
│       └── global/
│           ├── auth/        # JWT, Kakao 인증
│           ├── common/      # 공통 응답, 예외처리
│           ├── config/      # Security, JPA, Swagger 설정
│           └── scheduler/   # 알림 스케줄러
├── jobscope-frontend/       # React 프론트엔드
├── nginx/                   # Nginx 리버스 프록시
│   └── conf/
│       └── nginx.conf
└── docker-compose.yml
```

---

## 로컬 실행 방법

### 사전 요구사항

- Docker / Docker Compose
- (선택) JDK 21 — 백엔드 단독 실행 시

### 1. 환경변수 설정

`.env` 파일을 생성하고 필요한 값을 채웁니다.

> 필요한 환경변수 목록은 프로젝트 관리자에게 문의하세요.

### 2. 실행

```bash
docker-compose up --build
```

| 서비스 | 주소 |
|--------|------|
| 프론트엔드 | http://localhost |
| Swagger UI | http://localhost/swagger-ui/index.html |
| 헬스체크 | http://localhost/actuator/health |

---

## API 개요

Base URL: `/api`  
인증 방식: `Authorization: Bearer {accessToken}`

| 도메인 | 엔드포인트 |
|--------|-----------|
| 인증 | `POST /api/auth/kakao`, `POST /api/auth/refresh`, `DELETE /api/auth/logout` |
| 유저 | `GET/PATCH/DELETE /api/users/me` |
| 지원 현황 | `GET/POST /api/applications`, `GET/PATCH/DELETE /api/applications/{id}` |
| 대시보드 | `GET /api/applications/dashboard` |
| 캘린더 | `GET /api/applications/calendar` |
| 전형 히스토리 | `POST/PATCH/DELETE /api/applications/{id}/histories/{historyId}` |
| 알림 로그 | `GET /api/alarms` |

---

## Phase 로드맵

| Phase | 내용 | 상태 |
|-------|------|------|
| Phase 1 | MVP (현재) | 개발 중 |
| Phase 2 | 합격·불합격 패턴 분석, 전형 소요 기간 통계 | 예정 |
| Phase 3 | 취준 스터디 그룹, 회사별 채용 인사이트 | 예정 |
| Phase 4 | B2B (부트캠프·학원 전용 대시보드) | 예정 |