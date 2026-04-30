# JobScope

취준생/이직자가 여러 회사 지원 현황을 한 곳에서 관리하는 서비스

---

## 서비스 소개

노션/엑셀로 직접 만들어 쓰던 지원 관리를 취준에 특화된 형태로 제공합니다.
어떤 회사가 어느 단계인지, 다음 면접이 언제인지를 한눈에 확인할 수 있습니다.

### 핵심 기능

**Phase 1 MVP (완료)**
- 지원 현황 CRUD — 회사명, 직군, 지원 날짜, 마감일, 공고 URL, 메모 등록
- 전형 단계별 히스토리 — 서류 → 코테 → 1차 면접 → 최종 단계별 결과 기록
- 대시보드 — 전체 현황 카운트, 이번 주 면접 일정, 마감 임박 공고 (D-3 이내)
- 캘린더 — 면접 일정·서류 마감일 시각화 (FullCalendar), 테마 5종, 다크모드 대응
- 카카오 소셜 로그인
- 카카오 알림톡 — D-7, D-3, D-1, D-Day 자동 발송 (매일 오전 9시 KST)
- 알림 이력 조회 및 선택 삭제

**Phase 2 인사이트 (완료)**
- 분석 화면 — 단계별 통과율 퍼널, 구간별 소요 기간, 직군 필터
- 대시보드 넛지 — 히스토리 미입력 지원건 안내
- MY 취준 여정 뷰 — 취준 기간, 단계별 퍼널, 월별 타임라인
- 지원현황 편집 모드 + 다중 선택 삭제
- 마감 임박 카드 드래그앤드롭 (순서 localStorage 유지)
- 다크모드 (MY 설정에서 토글)
- 캘린더 테마 스위처 5종

**Phase 2.5 관리자 페이지 (완료)**
- 관리자 권한 체계 (UserRole: USER/ADMIN, JWT role claim)
- 공지사항 CRUD (관리자) / 최신 공지 조회 (사용자 MY 페이지 벨 아이콘)
- 서비스 현황 통계 — 가입자 수, 7일 추이, 알림 현황, 평균 지원 수, 상위 기업
- 사용자 목록 조회·삭제 (인라인 2단계 확인)

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
| Migration | Flyway |
| Docs | Springdoc OpenAPI (Swagger, prod 비활성화) |
| Scheduler | Spring Scheduler |
| Build | Gradle + Checkstyle |

### 프론트엔드
| 항목 | 내용 |
|------|------|
| Library | React 19 |
| Language | JavaScript |
| 상태관리 | Zustand |
| 캘린더 | FullCalendar |
| HTTP | Axios |
| 아이콘 | lucide-react |
| 폰트 | Pretendard |
| 기타 | PWA (vite-plugin-pwa) |

### 인프라
| 항목 | 내용 |
|------|------|
| 서버 | AWS EC2 (Amazon Linux, t3.micro) |
| DB | MySQL 8.0 (Docker) |
| Storage | AWS S3 (프로필 이미지) |
| 프록시 | Nginx + HTTPS (Let's Encrypt) |
| 컨테이너 | Docker / Docker Compose |
| Registry | Docker Hub (2000jch/jobscope-*) |
| CI | GitHub Actions (Checkstyle + Test + MySQL 마이그레이션 검증) |
| CD | GitHub Actions (main 머지 시 EC2 자동 배포) |

---

## 프로젝트 구조

```
jobscope/
├── jobscope-backend/        # Spring Boot 백엔드
│   └── src/main/java/com/jobscope/
│       ├── domain/
│       │   ├── user/        # 유저 도메인 (카카오 로그인, 프로필, JWT)
│       │   ├── application/ # 지원 현황 도메인 (CRUD, 히스토리, 대시보드, 캘린더)
│       │   ├── alarm/       # 알림 도메인 (스케줄러, 이력)
│       │   ├── oauth/       # 카카오 OAuth 토큰 관리
│       │   ├── analytics/   # 분석 도메인 (통과율, 소요 기간)
│       │   ├── notice/      # 공지사항 도메인
│       │   └── admin/       # 관리자 도메인 (통계, 사용자 관리)
│       └── global/
│           ├── alarm/       # 카카오 메시지 발송 서비스
│           ├── auth/        # JWT, 카카오 인증
│           ├── common/      # 공통 응답, 예외처리
│           ├── config/      # Security, JPA, Swagger 설정
│           └── scheduler/   # 알림 스케줄러
├── jobscope-frontend/       # React 프론트엔드
│   └── src/
│       ├── api/             # Axios 인스턴스 및 도메인별 API 함수
│       ├── components/      # 재사용 컴포넌트 (alarm, application, analytics, admin, common, dashboard, mypage)
│       ├── hooks/           # 커스텀 훅
│       ├── pages/           # 페이지 컴포넌트 (Dashboard, Application, Calendar, Analytics, MyPage, Admin)
│       ├── stores/          # Zustand 스토어
│       └── utils/           # 유틸 함수
├── nginx/                   # Nginx 리버스 프록시
│   └── conf/nginx.conf
├── docker-compose.yml           # 운영 환경
└── docker-compose.local.yml     # 로컬 테스트 환경 (SSL 없음)
```

---

## 로컬 실행 방법

### 사전 요구사항

- Docker / Docker Compose
- (선택) JDK 21 — 백엔드 단독 실행 시

### 1. 환경변수 설정

`.env` 파일을 생성하고 필요한 값을 채웁니다.

```env
# Database
DB_ROOT_PASSWORD=
DB_NAME=
DB_USERNAME=
DB_PASSWORD=

# JWT
JWT_SECRET=
JWT_ACCESS_EXPIRE=
JWT_REFRESH_EXPIRE=

# Kakao OAuth
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
KAKAO_REDIRECT_URI=

# AWS S3 (프로필 이미지)
AWS_ACCESS_KEY=
AWS_SECRET_KEY=
AWS_S3_BUCKET=
AWS_REGION=

# Frontend
VITE_KAKAO_CLIENT_ID=
VITE_KAKAO_REDIRECT_URI=
```

### 2. 실행

```bash
# 로컬 테스트 (SSL 없음, 전체 스택)
docker-compose -f docker-compose.local.yml up --build

# 운영 환경 구성 (HTTPS 인증서 필요)
docker-compose up --build
```

| 서비스 | 주소 |
|--------|------|
| 프론트엔드 | http://localhost |
| 헬스체크 | http://localhost/actuator/health |

> Swagger UI는 prod 프로파일에서 비활성화됨. 로컬(default 프로파일)에서만 접근 가능: http://localhost:8080/swagger-ui/index.html

---

## API 개요

Base URL: `/api`  
인증 방식: `Authorization: Bearer {accessToken}`

| 도메인 | 엔드포인트 |
|--------|-----------|
| 인증 | `POST /api/auth/kakao`, `POST /api/auth/refresh`, `DELETE /api/auth/logout` |
| 유저 | `GET/PATCH/DELETE /api/users/me`, `POST/DELETE /api/users/me/profile-image` |
| 지원 현황 | `GET/POST /api/applications`, `GET/PATCH/DELETE /api/applications/{id}` |
| 대시보드 | `GET /api/applications/dashboard` |
| 캘린더 | `GET /api/applications/calendar` |
| 전형 히스토리 | `POST/PATCH/DELETE /api/applications/{id}/histories/{historyId}` |
| 알림 이력 | `GET /api/alarms`, `DELETE /api/alarms` |
| 분석 | `GET /api/analytics` |
| 취준 여정 | `GET /api/users/me/journey` |
| 공지사항 | `GET /api/notices/latest` |
| 관리자 공지 | `GET/POST /api/admin/notices`, `PATCH/DELETE /api/admin/notices/{id}` |
| 관리자 | `GET /api/admin/stats`, `GET/DELETE /api/admin/users/{id}` |

---

## 라우팅 구조

| 경로 | 페이지 | 인증 |
|------|--------|------|
| `/` | MainPage | 공개 |
| `/oauth/callback/kakao` | KakaoCallbackPage | 공개 |
| `/dashboard` | DashboardPage | 보호 |
| `/applications` | ApplicationPage | 보호 |
| `/calendar` | CalendarPage | 보호 |
| `/analytics` | AnalyticsPage | 보호 |
| `/mypage` | MyPage | 보호 |
| `/admin` | AdminPage | ADMIN 전용 |

---

## Phase 로드맵

| Phase | 내용 | 상태 |
|-------|------|------|
| Phase 1 | MVP — 지원 현황 CRUD, 캘린더, 알림 | ✅ 완료 |
| Phase 2 | 인사이트 — 통과율 분석, 소요 기간, 취준 여정, 다크모드 | ✅ 완료 |
| Phase 2.5 | 관리자 페이지 — 공지사항, 사용자 관리, 서비스 통계 | ✅ 완료 |
| Phase 3 | 취준 타임라인 리포트 공유 | 예정 |
| Phase 4 | 합격자 여정 아카이브, B2B | 예정 |