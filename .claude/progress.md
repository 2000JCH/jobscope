# 진행 상태

## 현재 단계
**Phase 2.5 운영 배포 완료 — backlog UI/기능 이슈 수정 후 정식 사용자 배포 예정**

---

## Phase 1 완료 항목

| 단위 | 내용 |
|------|------|
| 0 | Global 기반 (BaseEntity, ApiResponse, ErrorCode, JpaConfig, SwaggerConfig, Checkstyle) |
| 1 | 환경설정 (Dockerfile, docker-compose, React 초기 설정, PWA) |
| 2 | User (카카오 로그인, JWT, 마이페이지, 프로필 이미지 업로드·초기화 — AWS S3) |
| 3 | Application (지원 현황 CRUD, 히스토리, 대시보드, 캘린더) |
| 4 | Alarm (알림 스케줄러, 이력 조회·삭제) |
| 배포 | AWS EC2 + nginx HTTPS + GitHub Actions CD |
| 보안 | Swagger 운영 환경 비활성화 (prod 프로파일), NoResourceFoundException 404 처리 |
| 로컬 | docker-compose.local.yml — SSL 없이 전체 스택 로컬 테스트 환경 구성 |

---

## Phase 2 완료 항목

| 단위 | 내용 |
|------|------|
| 백엔드 1 | Analytics API — `GET /api/analytics` (신뢰 지표 + 퍼널 + 구간별 소요 기간) |
| 백엔드 2 | Dashboard 넛지 — `DashboardResponse`에 `unstatedCount` 추가. 마감일 경과 후에만 표시 |
| 백엔드 3 | MY 취준 여정 API — `GET /api/users/me/journey` (JourneyResponse, MonthlyTimelineResponse) |
| 프론트 4 | AnalyticsPage — 신뢰 지표 + 퍼널 차트 + 소요 기간 + 직군 필터 |
| 프론트 5 | Dashboard 넛지 카드 (NudgeCard) |
| 프론트 6 | MY 취준 여정 뷰 (JourneySection) |
| 추가 | BottomNav 5탭 (홈/지원현황/캘린더/분석/MY) |
| 추가 | 지원현황 편집 모드 + 다중 선택 삭제 |
| 추가 | 지원현황 + 홈 마감임박 드래그앤드롭 (localStorage 순서 유지) |
| 추가 | 다크모드 (MY 설정에서 토글, localStorage 저장) |
| 추가 | 캘린더 테마 스위처 5종 (MY 설정에서 선택, localStorage 저장) |
| 추가 | 캘린더 다크모드 대응 (FullCalendar CSS 오버라이드) |
| 수정 | 분석 페이지 코테 필터 제거 (모든 직군 대상 서비스임을 반영) |
| 수정 | 퍼널 최대 이탈 단계 색상 빨강 → 주황 |
| 수정 | 홈 마감임박 dDay null일 때 "—" 노출 제거 |

---

## Phase 2.5 완료 항목

| 단위 | 내용 |
|------|------|
| 백엔드 1 | UserRole enum (USER/ADMIN), User 엔티티에 role + last_login_at 컬럼 추가 |
| 백엔드 2 | JWT 토큰에 role claim 추가 (JwtProvider, JwtFilter), SecurityConfig 어드민 권한 설정 |
| 백엔드 3 | Notice 도메인 — 공지사항 CRUD API (관리자용 `/api/admin/notices`, 사용자용 `/api/notices/latest`) |
| 백엔드 4 | Admin 도메인 — 서비스 현황 통계 API (`GET /api/admin/stats`) |
| 백엔드 5 | Admin 도메인 — 사용자 목록 조회·삭제 API (`GET/DELETE /api/admin/users`) |
| 프론트 6 | AdminRoute (ADMIN 권한 가드), `/admin` 라우트 추가 |
| 프론트 7 | AdminPage — 3탭 구성 (통계 / 사용자 / 공지사항) |
| 프론트 8 | AdminStatsSection — 사용자 카드 + 7일 추이 테이블 + 알림 카드 + 평균 지원 수 + 상위 기업 |
| 프론트 9 | AdminUserTable — 페이지네이션 + 인라인 2단계 삭제 확인 |
| 프론트 10 | AdminNoticeList / AdminNoticeForm — 공지 생성·수정·삭제 |
| 프론트 11 | ~~NoticeBanner~~ → 공지 벨 아이콘 — MY 페이지 헤더 벨 아이콘 (미읽음 뱃지, 패널 펼침, localStorage 읽음 처리) |
| 프론트 12 | MyPage 어드민 버튼 (ADMIN 유저에게만 표시) |

---

## UI 개선 (2026-04-24)

| 항목 | 내용 |
|------|------|
| MY 페이지 프로필 UI 개선 | "프로필" 섹션 헤더 제거. 프로필 사진 옆 닉네임(크게) + "내 정보 보기" 토글 버튼 배치. 저장 성공 시 폼 자동 닫힘 (수정 항목 1개 기준) |
| KakaoAuthService 임시 로그 제거 | `getKakaoUserInfo` 내 `kakao_account` 출력 로그 제거 |
| 지원현황 검색 UX 개선 | 상시 노출 검색창 → 필터 행 우측 돋보기 아이콘으로 교체. 클릭 시 헤더가 검색바로 전환(B패턴). 엔터 입력 시에만 API 호출 (키 입력마다 호출 제거) |

---

## 운영 환경 정보

- 도메인: https://jscope.duckdns.org
- EC2: Amazon Linux, t3.micro, IP 3.39.177.234
- 프로젝트 위치: ~/jobscope (EC2)
- HTTPS 인증서 만료: 2026-07-17 (자동 갱신 설정됨)
- CD 트리거: main 브랜치 머지 시 자동 배포
- Docker Hub: 2000jch/jobscope-backend, frontend, nginx

---

## 로컬 테스트 환경

- `docker-compose.local.yml` — nginx SSL 없이 전체 스택(MySQL + 백엔드 + 프론트엔드 + nginx) 로컬 실행 가능
- 실행 명령: `docker-compose -f docker-compose.local.yml up --build`
- 접속: http://localhost
- prod 환경과 차이: JPA_DDL_AUTO=validate, SPRING_PROFILES_ACTIVE=default, COOKIE_SECURE=false
- Flyway 도입 후 JPA_DDL_AUTO=update → validate로 변경됨 (스키마는 Flyway가 관리)

---

## 주요 결정 사항

- 브랜치 전략: feat/{domain}-backend, feat/{domain}-frontend → develop → main
- 머지 전략: feat/* → develop은 Merge commit, develop → main은 Squash and merge
- 경량 DDD: 비즈니스 로직은 Entity에, Service는 흐름 조율만
- refreshToken httpOnly 쿠키 관리 (localStorage XSS 방지)
- 카카오 알림: 나에게 보내기 방식 (무료, oauth_token 사용)
- @SQLRestriction은 @MappedSuperclass 상속 안 됨 → 엔티티에 직접 선언 필수 (Hibernate 6 버그)
- 코테 필터 제거 결정 (2026-04-21): 개발자 전용 서비스가 아닌 모든 직군 대상이므로 제거
- Flyway 마이그레이션 도입 (2026-04-23): DB 스키마 버전 관리 자동화, baseline-on-migrate로 기존 운영 DB 대응
- 공지 표시 UX 변경 (2026-04-23): 대시보드 상단 NoticeBanner → MY 페이지 헤더 벨 아이콘으로 대체. 최신 공지 1개(GET /api/notices/latest) 구조 유지

---

## 체크 필요

- [ ] 알림 운영 테스트 — 매일 오전 9시 스케줄러가 실제 카카오 메시지 발송하는지 확인
- [ ] 카카오 개발자 콘솔 닉네임 동의항목 활성화 (현재 비활성화 상태)
- [ ] EC2 운영 서버 ~/jobscope/.env에 KAKAO_ADMIN_KEY 추가 (main 머지 후 배포 전 처리)

---

## 라우팅 구조

| 경로 | 컴포넌트 | 인증 |
|------|----------|------|
| `/` | MainPage | 공개 |
| `/oauth/callback/kakao` | KakaoCallbackPage | 공개 |
| `/dashboard` | DashboardPage + BottomNav | 보호 |
| `/applications` | ApplicationPage + BottomNav | 보호 |
| `/calendar` | CalendarPage + BottomNav | 보호 |
| `/analytics` | AnalyticsPage + BottomNav | 보호 |
| `/mypage` | MyPage + BottomNav | 보호 |
| `/admin` | AdminPage | ADMIN 전용 |

---

## 다음 할 일

1. **정식 배포 전 backlog 이슈 수정** (backlog.md 참고)
   - UI/UX 개선 및 기능 이슈 수정 후 사용자에게 배포

2. **Phase 3 — 취준 타임라인 리포트** (사용자 요청 시)
   - 상세 내용은 `phase-roadmap.md` 참고