# 포트폴리오 이슈 기록

> 이력서/포트폴리오 작성을 위한 기술적 문제 해결 기록  
> 코딩 중 의미 있는 이슈만 선별 — 문제 정의 → 원인 → 해결 → 성과 구조로 작성

---

## [AUTH-001] KakaoCallbackPage 중복 실행 방지 (React StrictMode 대응)

**문제**  
React StrictMode 환경에서 `useEffect`가 두 번 실행되어 카카오 인가코드로 토큰을 중복 요청 → 카카오 서버에서 이미 사용된 코드 오류 발생

**원인**  
카카오 인가코드는 1회용이라 두 번째 요청 시 서버에서 오류 반환. React StrictMode는 개발 환경에서 의도적으로 Effect를 두 번 실행함

**해결**  
`useRef`로 실행 여부 플래그를 관리하여 최초 1회만 실행 보장

```js
const called = useRef(false);

useEffect(() => {
  if (called.current) return;
  called.current = true;
  // 카카오 로그인 처리
}, []);
```

**효과**  
- StrictMode 환경에서도 인가코드 중복 사용 오류 없음  
- `useState`가 아닌 `useRef` 사용 → 플래그 변경 시 리렌더링 미발생

---

## [AUTH-002] refreshToken XSS 취약점 제거 — httpOnly 쿠키 전환

**문제**
refreshToken을 Zustand persist(localStorage)에 저장하면 XSS 공격 시 JS로 탈취 가능. refreshToken은 장기 유효 토큰이라 탈취 시 지속적 인증 도용이 가능

**해결**
refreshToken을 localStorage에서 제거하고 서버가 httpOnly 쿠키로 내려주도록 전환

백엔드:
```java
// 로그인 응답 시 쿠키 설정
ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
    .httpOnly(true)
    .secure(cookieSecure)   // 운영: true, 로컬: false
    .sameSite("Lax")
    .path("/api/auth")      // auth 경로에서만 쿠키 전송
    .maxAge(refreshExpireMs / 1000)
    .build();

// refresh 요청 시 쿠키에서 자동 읽기
@CookieValue(name = "refresh_token") String refreshToken
```

프론트엔드:
```js
// refreshToken 코드 완전 제거, 쿠키 자동 전송
axios.create({ withCredentials: true })
```

**포인트**
- `path("/api/auth")` 로 경로 제한 → 다른 API 요청에는 쿠키 미전송 (최소 권한)
- `COOKIE_SECURE` 환경변수로 로컬(false)/운영(true) 분기
- 프론트엔드 코드에서 refreshToken이 완전히 사라져 탈취 경로 자체를 제거

---

---

## [AUTH-003] 동시 401 요청 시 토큰 refresh 중복 방지 (isRefreshing 패턴)

**문제**
accessToken 만료 시 여러 API 요청이 동시에 401을 받으면, 각 요청이 독립적으로 refresh를 시도함 → 첫 번째 refresh 이후 이전 refreshToken이 무효화되어 나머지 요청들이 모두 실패 → 강제 로그아웃 발생

**해결**
`isRefreshing` 플래그와 `failedQueue` 배열로 refresh 진행 중 요청을 대기시킨 후 일괄 재시도

```js
let isRefreshing = false;
let failedQueue = [];

// 401 발생 시
if (isRefreshing) {
  // refresh 중이면 큐에 대기
  return new Promise((resolve, reject) => {
    failedQueue.push({ resolve, reject });
  }).then((token) => {
    originalRequest.headers.Authorization = `Bearer ${token}`;
    return instance(originalRequest);
  });
}

isRefreshing = true;
try {
  // refresh 성공 → 대기 중인 요청 전부 재시도
  processQueue(null, newAccessToken);
} catch (err) {
  // refresh 실패 → 대기 중인 요청 전부 reject
  processQueue(err, null);
} finally {
  isRefreshing = false;
}
```

**효과**
- refresh 요청 1회로 동시 401 요청 전체 복구
- refresh 실패 시 대기 중인 요청도 일관되게 실패 처리 후 로그아웃

---

## [ALARM-001] 알림 발송 방식 전환 — 알림톡(유료) → 나에게 보내기(무료)

**문제**
카카오 알림톡은 건당 7.5원 과금. 사용자 1,000명 기준 일 최대 4,000건(D7/D3/D1/DEADLINE) × 7.5원 = 일 30,000원 발생.
MVP 서비스에서 비용 구조가 맞지 않아 무료 대안 탐색.

**원인 분석**
알림톡은 사전 심사받은 템플릿만 사용 가능한 B2B API이고, 나에게 보내기는 사용자 본인의 카카오 계정으로 메시지를 보내는 개인 API (무료, 일 1,000건 제한 — 사용자 계정 기준이므로 서비스 전체에 무관).

**해결**
카카오 나에게 보내기 API(`POST /v2/api/talk/memo/default/send`)로 전환.
사용자 Kakao access_token을 oauth_token 테이블에 저장하고 발송 시 활용.

**설계 포인트**

1. `oauth_token` 테이블 분리
   - users 테이블에 token 컬럼을 추가하면 users 쿼리마다 불필요한 토큰 데이터가 로딩됨
   - (user_id, provider) UNIQUE로 provider 확장에 대비 (KAKAO → 타 소셜 추후 추가 가능)

2. `OAuthTokenService` 단일 책임 분리
   - access_token 만료 검증 + refresh_token으로 자동 갱신 로직을 전담
   - `KakaoMessageService`가 갱신 로직을 인라인으로 갖지 않도록 분리하여 재사용성 확보

3. `AlarmService.sendAndLog()` propagation = REQUIRES_NEW 유지
   - 개별 알림 발송 실패가 같은 배치의 다른 알림에 영향 주지 않도록 독립 트랜잭션 보장

**Phase 1 허용 한계 (포트폴리오 개선 포인트)**
- oauth_token을 DB에 평문 저장 → Phase 2에서 AES 암호화 예정

**효과**
- 알림 발송 비용 0원
- 사용자별 일 1,000건 제한은 서비스 기준(스케줄러 1회/일)으로 실질적 제한 없음

---

> 개발 진행하면서 의미 있는 이슈 생기면 추가 예정