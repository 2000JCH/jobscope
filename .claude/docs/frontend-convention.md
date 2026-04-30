# 프론트엔드 컨벤션 문서

## 개요

- **언어** : JavaScript (TypeScript 미사용)
- **프레임워크** : React 19
- **빌드 도구** : Vite
- **연관 문서** : `architecture.md`, `api-design.md`

---

## 1. 폴더 구조

```
jobscope-frontend/src
├── api              # Axios 인스턴스 및 API 호출 함수
│   ├── axios.js     # Axios 인스턴스 설정
│   ├── auth.js
│   ├── user.js          # 프로필 조회·수정·삭제, 이미지 업로드·초기화, 취준 여정
│   ├── application.js   # fetchApplications: paramsSerializer로 배열 파라미터 직렬화
│   ├── alarm.js
│   ├── analytics.js     # GET /api/analytics
│   ├── notice.js        # GET /api/notices/latest
│   └── admin.js         # 관리자 통계·사용자·공지 API
├── components
│   ├── common
│   │   ├── ProtectedRoute.jsx   # 미인증 시 / 로 리다이렉트
│   │   ├── AdminRoute.jsx       # ADMIN 권한 가드
│   │   ├── BottomSheet.jsx      # lockCount 모듈 변수로 중첩 스크롤락 관리
│   │   ├── BottomNav.jsx        # NavLink 기반 5탭 (dashboard/applications/calendar/analytics/mypage)
│   │   └── ConfirmDialog.jsx
│   ├── application
│   │   ├── ApplicationCard.jsx
│   │   ├── ApplicationListFilter.jsx
│   │   ├── ApplicationFormBottomSheet.jsx
│   │   ├── ApplicationDetailBottomSheet.jsx
│   │   ├── HistoryTimeline.jsx / HistoryItem.jsx
│   │   ├── HistoryFormBottomSheet.jsx
│   │   └── ResultBadge.jsx / StageResultBadge.jsx
│   ├── alarm
│   │   └── AlarmLogItem.jsx     # 알림 이력 단건 (편집 모드 체크박스 지원)
│   ├── dashboard
│   │   ├── SummaryCards.jsx
│   │   ├── NudgeCard.jsx        # 히스토리 미입력 지원건 넛지
│   │   ├── WeekScheduleList.jsx
│   │   └── ImminentDeadlineList.jsx
│   ├── analytics
│   │   └── ...                  # 신뢰 지표, 퍼널 차트, 소요 기간 컴포넌트
│   ├── mypage
│   │   └── JourneySection.jsx   # 취준 여정 뷰 (월별 타임라인)
│   └── admin
│       ├── AdminStatsSection.jsx
│       ├── AdminUserTable.jsx
│       ├── AdminNoticeList.jsx
│       └── AdminNoticeForm.jsx
├── pages
│   ├── MainPage.jsx             # / — 서비스 소개, 카카오 로그인 버튼 (scope=talk_message 포함)
│   ├── KakaoCallbackPage.jsx    # /oauth/callback/kakao — 카카오 OAuth redirect 처리
│   ├── DashboardPage.jsx        # /dashboard
│   ├── ApplicationPage.jsx      # /applications — 지원 현황 CRUD
│   ├── CalendarPage.jsx         # /calendar — FullCalendar (endDate off-by-one 보정, currentRange 기반 reload)
│   ├── AnalyticsPage.jsx        # /analytics — 분석 (신뢰 지표, 퍼널, 소요 기간, 직군 필터)
│   ├── MyPage.jsx               # /mypage — 프로필 수정, 공지 벨 아이콘, 취준 여정, 알림 이력, 계정 관리
│   └── AdminPage.jsx            # /admin — 관리자 전용 (통계/사용자/공지 3탭)
├── stores
│   ├── useAuthStore.js
│   └── useApplicationStore.js
├── hooks
│   ├── useApplicationList.js    # 검색/필터/정렬/페이지
│   ├── useApplicationDetail.js  # currentIdRef → reload() 시 id 불필요
│   ├── useDashboard.js
│   ├── useCalendarEvents.js
│   ├── useCalendarTheme.js      # 캘린더 테마 5종 (localStorage 저장)
│   ├── useTheme.js              # 다크모드 토글 (localStorage 저장)
│   ├── useAlarmLogs.js          # 알림 이력 목록 (페이지네이션, deleteAndReload)
│   ├── useAnalytics.js          # 분석 데이터 (직군 필터 포함)
│   ├── useJourney.js            # 취준 여정 데이터
│   ├── useNotice.js             # 최신 공지 조회 + hasUnread/markAsRead (localStorage 읽음 처리)
│   ├── useDragOrder.js          # 드래그앤드롭 순서 (localStorage 저장)
│   ├── useAdminStats.js
│   ├── useAdminUsers.js
│   └── useAdminNotices.js
├── utils
│   ├── dateFormat.js            # formatDate, formatDateTime, formatTime, formatDDay
│   └── applicationEnum.js      # RESULT_LABEL, STAGE_RESULT_LABEL, SORT_OPTIONS 등
└── assets
```

---

## 2. 네이밍 규칙

```
컴포넌트 파일   : PascalCase          예) ApplicationCard.jsx
일반 파일       : camelCase           예) useAuthStore.js
페이지 파일     : {Name}Page.jsx      예) DashboardPage.jsx
커스텀 훅       : use{Name}.js        예) useApplicationList.js
Zustand store  : use{Name}Store.js   예) useAuthStore.js
API 함수        : camelCase           예) createApplication(), fetchApplications()
```

---

## 3. 컴포넌트 규칙

```
- 함수형 컴포넌트만 사용 (클래스 컴포넌트 금지)
- 1파일 1컴포넌트 원칙
- props drilling 3단계 이상 시 Zustand store로 전환
- 페이지 컴포넌트는 pages/ 에만, 재사용 컴포넌트는 components/ 에만
```

---

## 4. Axios 인스턴스 규칙

```javascript
// api/axios.js
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  withCredentials: true,  // httpOnly 쿠키(refreshToken) 자동 전송
});

// 요청 인터셉터: Authorization 헤더 자동 주입
// 응답 인터셉터: 401 → 토큰 재발급 후 재시도, 실패 시 로그아웃
// 동시 401 처리: isRefreshing + failedQueue 패턴으로 refresh 중복 방지
```

```
- API 함수는 반드시 api/ 폴더 내 도메인별 파일로 분리
- 컴포넌트에서 axios 직접 호출 금지 — 반드시 api/ 함수 경유
- 에러 처리는 인터셉터에서 공통 처리
```

---

## 5. Zustand store 규칙

```javascript
// stores/useAuthStore.js
const useAuthStore = create(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      // refreshToken은 httpOnly 쿠키로 관리 — store에 저장 안 함
      setAuth: (user, accessToken) => set({ user, accessToken }),
      setAccessToken: (accessToken) => set({ accessToken }),
      logout: () => set({ user: null, accessToken: null }),
    }),
    { name: 'auth-storage', partialize: (state) => ({ user: state.user, accessToken: state.accessToken }) }
  )
);
```

```
- store 파일명: use{Domain}Store.js
- 서버 상태(목록, 상세)는 store 대신 컴포넌트 로컬 state 또는 커스텀 훅으로 관리
- 전역 상태만 store에 저장: 인증 정보, 전역 UI 상태
```

---

## 6. 환경변수 규칙

```
Vite 환경변수는 VITE_ 접두사 필수
예) VITE_API_BASE_URL, VITE_KAKAO_CLIENT_ID

import.meta.env.VITE_API_BASE_URL 형태로 접근
.env 파일 직접 수정 금지 — 개발자가 직접 관리
```

---

## 7. PWA 규칙

```
- vite-plugin-pwa 사용 (registerType: 'autoUpdate')
- manifest는 vite.config.js 내 VitePWA 플러그인 옵션으로 관리 (public/manifest.json 수동 생성 금지)
- Service Worker는 빌드 시 자동 생성 (/sw.js)
- 아이콘: public/favicon.svg 재사용 → 디자인 확정 후 PNG(192, 512)로 교체
- theme_color: #FEE500 (카카오 옐로우)
- display: standalone (앱처럼 전체화면)
- 이후 수정 최소화
```

---

## 8. 모바일 우선(Mobile-First) CSS 규칙

```
- 기본 스타일은 모바일 기준으로 작성 (최소 320px 기준)
- #root max-width: 430px; margin: 0 auto — 모바일 PWA 컨테이너 고정
- 화면이 넓어질 때 min-width 미디어 쿼리로 확장
  예) @media (min-width: 768px) { /* 태블릿 이상 */ }
      @media (min-width: 1024px) { /* 데스크탑 이상 */ }
- max-width 기반 미디어 쿼리 사용 금지
- 고정 px 대신 rem 사용 권장 (기준: 1rem = 16px)
- 터치 타겟 최소 44×44px 유지 (버튼, 링크, 입력 필드)
- viewport 단위: height에는 dvh 우선 사용 (iOS 주소창 대응)
  예) height: 100dvh  (not 100vh)
- iOS safe area 여백 고려
  예) padding-bottom: env(safe-area-inset-bottom)
```

---

## 9. CSS 디자인 토큰 규칙

모든 색상, 간격, 그림자 값은 `src/index.css`에 정의된 CSS 변수를 사용한다.
하드코딩된 hex 색상 값을 CSS 모듈에 직접 작성하지 않는다.

```css
/* 색상 */
--color-primary: #2563eb          /* 메인 파란색 */
--color-primary-light: #eff6ff    /* 파란색 배경 (칩 활성, 강조 배경) */
--color-bg: #ffffff                /* 페이지 배경 */
--color-surface: #f8fafc          /* 카드 배경 */
--color-surface-2: #f1f5f9        /* 비활성 카드 배경 */
--color-border: #e2e8f0           /* 테두리 */
--color-text: #0f172a             /* 본문 텍스트 */
--color-text-sub: #475569         /* 보조 텍스트 */
--color-text-muted: #94a3b8       /* 흐린 텍스트 */
--color-success / --color-success-bg
--color-danger  / --color-danger-bg
--color-warning / --color-warning-bg

/* 모서리 */
--radius-sm: 0.5rem  --radius-md: 0.75rem  --radius-lg: 1rem  --radius-xl: 1.25rem

/* 그림자 */
--shadow-sm / --shadow-md / --shadow-lg
```

---

## 10. 아이콘 규칙

- 아이콘은 `lucide-react` 라이브러리만 사용 (이모지 아이콘 사용 금지)
- 설치: `npm install lucide-react --legacy-peer-deps` (vite-plugin-pwa 피어 의존성 충돌 대응)
- 사용: `import { IconName } from 'lucide-react'`
- BottomNav 아이콘: LayoutDashboard / ClipboardList / Calendar / User

---

## 11. 폰트 규칙

- Pretendard 폰트를 기본 폰트로 사용
- CDN: `@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard-dynamic-subset.min.css')`
- 적용: `--font-main: 'Pretendard', system-ui, -apple-system, BlinkMacSystemFont, sans-serif`
- 모든 입력 요소, 버튼에 `font-family: var(--font-main)` 명시 (브라우저 기본값 상속 안 됨)