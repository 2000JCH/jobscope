# 주의사항 및 추후 개선 사항

---

## 🔐 보안

### [SEC-001] refreshToken httpOnly 쿠키 전환 ✅ 해결됨 (단위 2)
- **내용**: refreshToken을 localStorage에 평문 저장 → XSS 공격 시 탈취 가능
- **해결**: httpOnly 쿠키로 전환 (JS에서 접근 불가)
- **관련 파일**: AuthController, SecurityConfig, axios.js, useAuthStore.js

### [SEC-002] 운영 배포 시 쿠키 Secure 플래그 활성화 필요
- **내용**: 로컬 개발 환경에서는 `Secure=false`로 설정 (HTTP). 운영 환경(HTTPS) 배포 시 반드시 `Secure=true`로 변경해야 쿠키가 HTTPS에서만 전송됨
- **조치**: 운영 배포 전 환경변수 또는 Spring Profile로 분기 처리

### [SEC-003] CORS allowedOrigins 운영 도메인으로 변경 필요
- **내용**: 현재 `CORS_ALLOWED_ORIGINS` 환경변수 기본값이 `http://localhost`. 운영 배포 시 실제 도메인으로 변경 필요
- **조치**: .env에 운영 도메인 설정 (`https://jobscope.도메인`)

---

## 📦 의존성

### [DEP-001] vite-plugin-pwa Vite 8 미지원 — --legacy-peer-deps 임시 처리
- **내용**: `vite-plugin-pwa@0.21.x`의 peer dependency가 `vite ^3~6`까지만 선언되어 Vite 8과 충돌
- **현재 조치**: `jobscope-frontend/Dockerfile`에 `npm ci --legacy-peer-deps` 적용
- **향후 조치**: `vite-plugin-pwa`가 Vite 8 공식 지원 버전 출시 시 업그레이드 후 `--legacy-peer-deps` 제거

---

## ⚙️ 인프라

### [INF-001] Redis 도입 시 refreshToken 저장소 전환
- **내용**: 현재 refreshToken을 DB(User 테이블 또는 별도 테이블)에 저장. 트래픽 증가 시 Redis로 전환하면 만료 관리 및 성능 개선
- **조치**: Phase 2 이후 사용자 증가 추이 보고 결정

### [INF-002] 운영 환경 docker-compose 분리
- **내용**: 현재 docker-compose.yml은 로컬 개발 전용. 운영 배포 시 `docker-compose.prod.yml` 별도 작성 필요 (Secure 플래그, 도메인, HTTPS 설정 등)
- **조치**: 운영 배포 시점에 작성

---

## 🧪 테스트

### [TEST-001] 카카오 OAuth 전체 플로우 통합 테스트 ✅ 해결됨 (단위 3)
- **내용**: 카카오 클라이언트 ID/시크릿 미등록 상태로 실제 로그인 플로우 테스트 미수행
- **해결**: docker-compose up --build 후 카카오 로그인 → DB 유저 저장 확인 완료

### [TEST-003] @Value 설정 추가 시 테스트 properties 더미값 누락 (3회 이상 반복)
- **내용**: `application.yml`에 새 `@Value` 항목 추가 후 `src/test/resources/application.properties`에 더미값을 추가하지 않으면 `contextLoads()` 테스트가 `PlaceholderResolutionException`으로 실패
- **해결**: `application.properties`에 `해당키=test-더미값` 형태로 추가
- **예방**: CLAUDE.md 절대 규칙 18번에 등록 — @Value 추가 시 테스트 properties 동시 수정 필수

### [TEST-002] 토큰 만료 시나리오 수동 테스트 필요
- **내용**: accessToken 만료 → refresh → 재시도 플로우, 동시 401 요청 처리(isRefreshing 패턴) 실제 동작 검증 필요
- **조치**: 카카오 키 등록 후 accessToken TTL을 짧게 설정하여 수동 확인