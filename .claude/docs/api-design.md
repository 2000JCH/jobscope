# API 설계 문서

## 개요

- **Base URL** : `/api`
- **인증 방식** : Bearer Token (JWT) — `Authorization: Bearer {accessToken}` 헤더
- **응답 형식** : JSON
- **날짜 형식** : ISO 8601 (`yyyy-MM-dd`, `yyyy-MM-dd'T'HH:mm:ss`)
- **시간 기준** : KST 고정 (타임존 변환 없음, 글로벌 서비스 아님)
- **연관 문서** : `db-design.md`, `architecture.md`

---

## 공통 응답 구조

> 모든 API는 아래 구조를 따름. 별도 명시 없으면 `data` 필드 안에 실제 데이터가 들어감

```json
// 성공
{ "success": true, "data": { } }

// 실패
{ "success": false, "error": { "code": "ERROR_CODE", "message": "에러 메시지" } }

// 페이지네이션 목록
{
  "success": true,
  "data": {
    "content": [ ],
    "totalElements": 42,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

**HTTP 상태코드 규칙**
| 메서드 | 상태코드 |
|---|---|
| `GET` | 200 OK |
| `POST` | 201 Created |
| `PATCH` | 200 OK |
| `DELETE` | 200 OK |

---

## 에러 코드 정의

| 코드                    | HTTP | 발생 상황                                  |
| ----------------------- | ---- | ------------------------------------------ |
| `INVALID_TOKEN`         | 401  | JWT 토큰 유효하지 않음                     |
| `TOKEN_EXPIRED`         | 401  | JWT 토큰 만료                              |
| `FORBIDDEN`             | 403  | 본인 리소스가 아님 (타인 데이터 접근 시도) |
| `USER_NOT_FOUND`        | 404  | 유저 없음                                  |
| `APPLICATION_NOT_FOUND` | 404  | 지원 정보 없음 또는 Soft Delete된 항목     |
| `HISTORY_NOT_FOUND`     | 404  | 전형 단계 없음                             |
| `INVALID_REQUEST`       | 400  | 요청 값 검증 실패 (Bean Validation)        |
| `KAKAO_AUTH_FAILED`     | 502  | 카카오 인증 서버 오류                      |
| `ALARM_SEND_FAILED`     | 502  | 카카오 나에게 보내기 발송 실패             |
| `IMAGE_UPLOAD_FAILED`   | 502  | S3 이미지 업로드 실패                      |

---

## 1. 인증 (Auth)

### 엔드포인트 목록

| 메서드   | 경로                | 설명                                  | 인증 필요 |
| -------- | ------------------- | ------------------------------------- | --------- |
| `POST`   | `/api/auth/kakao`   | 카카오 인가코드로 JWT 발급            | ❌        |
| `POST`   | `/api/auth/refresh` | Refresh Token으로 Access Token 재발급 | ❌        |
| `DELETE` | `/api/auth/logout`  | 로그아웃 (Refresh Token 무효화)       | ✅        |

---

### POST /api/auth/kakao

**[흐름]**

1. 프론트에서 카카오 OAuth 인가코드를 받아 서버로 전달
2. 서버에서 카카오 API로 액세스 토큰 교환
3. 카카오 유저 정보 조회 후 USER 테이블 upsert (신규면 INSERT, 기존이면 닉네임·프로필 동기화)
4. 카카오 access_token, refresh_token을 OAUTH_TOKEN 테이블에 upsert (나에게 보내기 발송용)
5. JWT(AccessToken + RefreshToken) 발급 후 반환

**[Request Body]**
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `code` | String | ✅ | 카카오 OAuth 인가코드 |

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `accessToken` | String | JWT Access Token |
| `refreshToken` | String | JWT Refresh Token |
| `user.id` | Long | 내부 유저 ID |
| `user.nickname` | String | 카카오 닉네임 |
| `user.profileImage` | String | 카카오 프로필 이미지 URL |

---

### POST /api/auth/refresh

**[비즈니스 로직]**

- Refresh Token 검증 후 새 Access Token 발급
- Refresh Token 자체는 갱신하지 않음

**[Request Body]**
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `refreshToken` | String | ✅ | 기존 Refresh Token |

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `accessToken` | String | 새로 발급된 JWT Access Token |

---

### DELETE /api/auth/logout

**[비즈니스 로직]**

- 서버에 저장된 Refresh Token 무효화 처리
- 추후 Redis 도입 시 Redis에서 삭제

**[Response - data]** : null

---

## 2. 유저 (User)

### 엔드포인트 목록

| 메서드   | 경로                           | 설명                              | 인증 필요 |
| -------- | ------------------------------ | --------------------------------- | --------- |
| `GET`    | `/api/users/me`                | 내 프로필 조회                    | ✅        |
| `PATCH`  | `/api/users/me`                | 프로필 수정                       | ✅        |
| `DELETE` | `/api/users/me`                | 회원 탈퇴                         | ✅        |
| `POST`   | `/api/users/me/profile-image`  | 프로필 이미지 업로드 (S3)         | ✅        |
| `DELETE` | `/api/users/me/profile-image`  | 프로필 이미지 초기화 (카카오 기본) | ✅        |

**[연관 엔티티]** : `User`

---

### GET /api/users/me

**[Response - data]**
| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `id` | Long | ❌ | 유저 PK |
| `nickname` | String | ❌ | 닉네임 |
| `email` | String | ✅ | 카카오 이메일 (선택 동의 항목) |
| `profileImage` | String | ✅ | 프로필 이미지 URL (커스텀 이미지 우선, 없으면 카카오 기본) |
| `hasCustomProfileImage` | Boolean | ❌ | 커스텀 프로필 이미지 존재 여부 |

---

### PATCH /api/users/me

**[비즈니스 로직]**

- 변경할 필드만 포함 (null 필드는 업데이트하지 않음)

**[Request Body]**
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `nickname` | String | ❌ | 변경할 닉네임 |

**[Response - data]** : null

---

### DELETE /api/users/me

**[비즈니스 로직]**

- 유저 Hard Delete
- 커스텀 프로필 이미지가 있으면 S3에서 삭제
- 연관된 APPLICATION, APPLICATION_HISTORY, ALARM_LOG 모두 cascade 삭제

**[Response - data]** : null

---

### POST /api/users/me/profile-image

**[Request]** : `multipart/form-data`
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `image` | MultipartFile | ✅ | 업로드할 이미지 (jpg/png, 5MB 이하) |

**[비즈니스 로직]**

- S3 `profile/` 폴더에 UUID 파일명으로 업로드
- 기존 커스텀 이미지가 있으면 S3에서 삭제 후 교체

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `profileImage` | String | 새로 업로드된 S3 이미지 URL |

---

### DELETE /api/users/me/profile-image

**[비즈니스 로직]**

- S3에 저장된 커스텀 이미지 삭제
- `customProfileImage` 를 null로 초기화 → 카카오 기본 이미지로 복귀

**[Response - data]** : null

---

## 3. 지원 현황 (Application)

### 엔드포인트 목록

| 메서드   | 경로                          | 설명                    | 인증 필요 |
| -------- | ----------------------------- | ----------------------- | --------- |
| `GET`    | `/api/applications`           | 지원 목록 조회          | ✅        |
| `POST`   | `/api/applications`           | 지원 등록               | ✅        |
| `GET`    | `/api/applications/{id}`      | 지원 상세 조회          | ✅        |
| `PATCH`  | `/api/applications/{id}`      | 지원 수정               | ✅        |
| `DELETE` | `/api/applications/{id}`      | 지원 삭제 (Soft Delete) | ✅        |
| `GET`    | `/api/applications/dashboard` | 대시보드 데이터 조회    | ✅        |
| `GET`    | `/api/applications/calendar`  | 캘린더용 일정 조회      | ✅        |

**[연관 엔티티]** : `Application`, `ApplicationHistory`

**[공통 보안 규칙]**

- 모든 Application 조회/수정/삭제 시 `application.userId == 인증된 유저 ID` 검증 필수
- 불일치 시 `FORBIDDEN` 에러 반환

**[Soft Delete 정책]**

- `DELETE /api/applications/{id}` 호출 시 `APPLICATION.deleted_at` 에 현재 시각 기록
- 이후 해당 applicationId를 가진 `APPLICATION_HISTORY`는 모든 조회에서 자동 제외
- `ALARM_LOG`는 발송 이력 보존을 위해 삭제하지 않고 그대로 유지
- JPA 구현 시 `Application` 엔티티에 `@SQLRestriction("deleted_at IS NULL")` 적용

**[dDay 계산 규칙]**

- `deadlineAt`의 날짜 부분과 서버 현재 날짜(KST)의 차이를 일(day) 단위로 계산
- 시분초는 무시하고 날짜만 비교
- 예) `deadlineAt = 2025-03-24 23:59`, 현재 = `2025-03-24 10:00` → `dDay = 0`
- 예) `deadlineAt = 2025-03-25 23:59`, 현재 = `2025-03-24 10:00` → `dDay = 1`
- `deadlineAt`이 null이면 `dDay = null` 반환
- 마감일이 지난 경우 음수 반환

**['최신 히스토리' 기준]**

- 해당 Application에 속한 히스토리 중 `id`가 가장 큰 레코드를 최신으로 간주
- `created_at` 기준 사용 금지 (유저가 단계를 순서와 다르게 등록할 수 있음)
- 이 기준은 `currentStage` 계산, `PATCH /applications/{id}` result 변경 시 히스토리 업데이트 등 전체에서 통일 적용

**[알림 발송 규칙]**

- 알림 타입: D-7, D-3, D-1, D-Day 고정
- 발송 시각: 매일 오전 9시 (KST) 고정
- 발송 조건: `alarm_enabled = TRUE` 인 지원건만 대상
- 발송 방식: 카카오 "나에게 보내기" API (`oauth_token.access_token` 사용, 만료 시 자동 갱신)
- 중복 방지: DB 유니크 인덱스로 원천 차단 (db-design.md 참고)

---

### GET /api/applications

**[Query Parameters]**
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `search` | String | ❌ | - | 회사명 검색 (부분 일치) |
| `result` | String | ❌ | - | `IN_PROGRESS` \| `PASSED` \| `FAILED` (복수 가능) |
| `sort` | String | ❌ | `LATEST` | `LATEST`(최신순) \| `DEADLINE`(마감임박순) \| `UPDATED`(업데이트순) |
| `page` | int | ❌ | `0` | 페이지 번호 |
| `size` | int | ❌ | `20` | 페이지 크기 |

**[Response - data.content 단건]**
| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `id` | Long | ❌ | 지원 PK |
| `companyName` | String | ❌ | 회사명 |
| `jobPosition` | String | ❌ | 직군 |
| `appliedAt` | String | ❌ | 지원 날짜 (`yyyy-MM-dd`) |
| `deadlineAt` | String | ✅ | 서류 마감 일시 |
| `result` | String | ❌ | `IN_PROGRESS` \| `PASSED` \| `FAILED` |
| `currentStage` | String | ❌ | 현재 전형 단계명 (id 최댓값 히스토리의 stage) |
| `alarmEnabled` | Boolean | ❌ | 알림 ON/OFF 여부 |
| `dDay` | int | ✅ | 마감까지 남은 일수 (위 dDay 계산 규칙 따름) |

---

### POST /api/applications

**[비즈니스 로직]**

- `APPLICATION` INSERT와 `APPLICATION_HISTORY` INSERT를 하나의 트랜잭션으로 처리
- 지원 등록 시 `APPLICATION_HISTORY`에 `stage='서류'`, `stage_result='PENDING'` 자동 생성
- 이를 통해 `currentStage`가 항상 null이 아님을 보장
- `appliedAt` 미래 날짜 허용 (미리 공고 등록 패턴 지원, 별도 검증 없음)
- `alarmEnabled` 기본값 true

**[Request Body]**
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `companyName` | String | ✅ | 회사명 |
| `jobPosition` | String | ✅ | 직군 |
| `appliedAt` | String | ✅ | 지원 날짜 (`yyyy-MM-dd`, 미래 날짜 허용) |
| `deadlineAt` | String | ❌ | 서류 마감 일시 (null 허용) |
| `alarmEnabled` | Boolean | ❌ | 알림 ON/OFF (기본값: true) |
| `jobPostingUrl` | String | ❌ | 채용 공고 URL (null 허용) |
| `memo` | String | ❌ | 자유 메모 (null 허용) |

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Long | 생성된 Application PK |

---

### GET /api/applications/{id}

**[비즈니스 로직]**

- 상세 조회 시 `APPLICATION_HISTORY` 목록을 함께 반환 (별도 목록 API 없음)
- 히스토리는 `id` 오름차순 정렬 (등록 순서 기준 타임라인)

**[Response - data]**
| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `id` | Long | ❌ | 지원 PK |
| `companyName` | String | ❌ | 회사명 |
| `jobPosition` | String | ❌ | 직군 |
| `appliedAt` | String | ❌ | 지원 날짜 |
| `deadlineAt` | String | ✅ | 서류 마감 일시 |
| `dDay` | int | ✅ | 마감까지 남은 일수 (목록 조회와 동일 규칙) |
| `result` | String | ❌ | 최종 합불 결과 |
| `alarmEnabled` | Boolean | ❌ | 알림 ON/OFF 여부 |
| `jobPostingUrl` | String | ✅ | 채용 공고 URL |
| `memo` | String | ✅ | 자유 메모 |
| `retrospective` | String | ✅ | 탈락 회고 메모 |
| `histories` | Array | ❌ | 전형 단계 목록 (최소 1개 보장, id 오름차순) |
| `histories[].id` | Long | ❌ | 히스토리 PK |
| `histories[].stage` | String | ❌ | 전형 단계명 |
| `histories[].stageResult` | String | ❌ | `PENDING` \| `PASS` \| `FAIL` |
| `histories[].scheduledAt` | String | ✅ | 예정 일시 |
| `histories[].completedAt` | String | ✅ | 완료 일시 |
| `histories[].memo` | String | ✅ | 단계별 메모 |

---

### PATCH /api/applications/{id}

**[비즈니스 로직]**

- 변경할 필드만 포함 (null 필드는 업데이트하지 않음)
- `result`를 `PASSED` 또는 `FAILED`로 변경 시 아래를 하나의 트랜잭션으로 처리
    1. `APPLICATION.result` 업데이트
    2. 해당 Application의 히스토리 중 `id`가 가장 큰 레코드의 `stage_result` 동일 값으로 업데이트
    3. 해당 히스토리의 `completed_at`을 현재 시각(KST)으로 기록
- `result=FAILED` 일 때만 `retrospective` 필드 저장 허용
- `alarmEnabled=false` 로 변경 시 해당 지원건 알림 전체 중단 (이미 발송된 건은 유지)

**[Request Body]**
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `companyName` | String | ❌ | 회사명 |
| `jobPosition` | String | ❌ | 직군 |
| `appliedAt` | String | ❌ | 지원 날짜 |
| `deadlineAt` | String | ❌ | 서류 마감 일시 |
| `alarmEnabled` | Boolean | ❌ | 알림 ON/OFF (false로 변경 시 해당 지원건 알림 중단) |
| `jobPostingUrl` | String | ❌ | 채용 공고 URL |
| `memo` | String | ❌ | 자유 메모 |
| `result` | String | ❌ | `IN_PROGRESS` \| `PASSED` \| `FAILED` |
| `retrospective` | String | ❌ | 탈락 회고 메모 (result=FAILED 시에만 유효) |

**[Response - data]** : null

---

### DELETE /api/applications/{id}

**[비즈니스 로직]**

- `APPLICATION.deleted_at` 에 현재 시각 기록 (Soft Delete)
- `APPLICATION_HISTORY`는 Hard Delete 하지 않음, 조회에서만 제외
- `ALARM_LOG`는 발송 이력 보존을 위해 그대로 유지

**[Response - data]** : null

---

### GET /api/applications/dashboard

**[비즈니스 로직]**

- `thisWeekSchedules` 기준 : KST 기준 이번 주 월요일 00:00:00 ~ 일요일 23:59:59
- `imminentDeadlines` : `deadlineAt`이 현재 시각 이후이면서 D-3 이내인 건만 포함 (마감 지난 건 제외)
- Soft Delete된 APPLICATION은 모든 집계에서 제외

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `summary.total` | int | 전체 지원 수 |
| `summary.inProgress` | int | 진행 중인 지원 수 |
| `summary.passed` | int | 합격 수 |
| `summary.failed` | int | 탈락 수 |
| `thisWeekSchedules[].applicationId` | Long | 지원 PK |
| `thisWeekSchedules[].companyName` | String | 회사명 |
| `thisWeekSchedules[].stage` | String | 전형 단계명 |
| `thisWeekSchedules[].scheduledAt` | String | 예정 일시 |
| `imminentDeadlines[].applicationId` | Long | 지원 PK |
| `imminentDeadlines[].companyName` | String | 회사명 |
| `imminentDeadlines[].deadlineAt` | String | 마감 일시 |
| `imminentDeadlines[].dDay` | int | 마감까지 남은 일수 |

---

### GET /api/applications/calendar

**[비즈니스 로직]**

- FullCalendar가 뷰포트 기준으로 startDate, endDate를 직접 계산해서 전달
- 예) 3월 달력 렌더링 시 → `startDate=2025-02-24&endDate=2025-04-06`
- 이벤트 타입
    - `INTERVIEW` : `APPLICATION_HISTORY.scheduled_at` 기준 (면접, 코테 등)
    - `DEADLINE` : `APPLICATION.deadline_at` 기준 (서류 마감)

**[Query Parameters]**
| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `startDate` | String | ✅ | 조회 시작일 (`yyyy-MM-dd`) |
| `endDate` | String | ✅ | 조회 종료일 (`yyyy-MM-dd`) |

**[Response - data 단건]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `date` | String | 날짜 (`yyyy-MM-dd`) |
| `events[].type` | String | `INTERVIEW` \| `DEADLINE` |
| `events[].applicationId` | Long | 지원 PK |
| `events[].companyName` | String | 회사명 |
| `events[].label` | String | 표시 문구 (예: '1차 면접', '서류 마감') |
| `events[].time` | String | 시각 (`HH:mm`) |

---

## 4. 전형 히스토리 (Application History)

### 엔드포인트 목록

> 목록 조회는 `GET /api/applications/{id}` 상세 조회의 `histories` 필드로 대체
> 별도 목록 조회 API 없음

| 메서드   | 경로                                           | 설명           | 인증 필요 |
| -------- | ---------------------------------------------- | -------------- | --------- |
| `POST`   | `/api/applications/{id}/histories`             | 전형 단계 등록 | ✅        |
| `PATCH`  | `/api/applications/{id}/histories/{historyId}` | 전형 단계 수정 | ✅        |
| `DELETE` | `/api/applications/{id}/histories/{historyId}` | 전형 단계 삭제 | ✅        |

**[연관 엔티티]** : `Application`, `ApplicationHistory`

**[공통 보안 규칙]**

- `{id}`로 조회한 Application의 `userId == 인증된 유저 ID` 검증 필수
- `{historyId}`가 해당 `{id}` Application 소속인지 검증 필수
- 불일치 시 `FORBIDDEN` 에러 반환

---

### POST /api/applications/{id}/histories

**[비즈니스 로직]**

- 지원 등록 시 자동 생성된 '서류' 단계 외에 추가 단계 등록 시 사용
- `scheduledAt`이 있으면 해당 일시 기준으로 알림 스케줄 대상이 됨

**[Request Body]**
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `stage` | String | ✅ | 전형 단계명 (예: '코딩테스트', '1차 면접') |
| `stageResult` | String | ❌ | `PENDING` \| `PASS` \| `FAIL` (기본값: `PENDING`) |
| `scheduledAt` | String | ❌ | 예정 일시 (알림 트리거 기준) |
| `memo` | String | ❌ | 단계별 메모 |

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Long | 생성된 History PK |

---

### PATCH /api/applications/{id}/histories/{historyId}

**[비즈니스 로직]**

- 변경할 필드만 포함 (null 필드는 업데이트하지 않음)
- `completedAt` 처리 우선순위
    1. 요청에 `completedAt` 값이 있으면 그 값을 사용
    2. 요청에 `completedAt`이 없고 `stageResult`가 `PASS` 또는 `FAIL`로 변경되는 경우 현재 시각(KST) 자동 기록
    3. `stageResult` 변경 없이 다른 필드만 수정하는 경우 `completedAt` 변경 없음
- `stageResult`를 `PASS` 또는 `FAIL`로 변경 시
  해당 historyId가 해당 Application의 id 최댓값(최신 단계)인 경우
  APPLICATION.result도 트랜잭션 내 동시 업데이트
  PASS → APPLICATION.result = PASSED
  FAIL → APPLICATION.result = FAILED

**[Request Body]**
| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `stage` | String | ❌ | 전형 단계명 |
| `stageResult` | String | ❌ | `PENDING` \| `PASS` \| `FAIL` |
| `scheduledAt` | String | ❌ | 예정 일시 |
| `completedAt` | String | ❌ | 완료 일시 (있으면 우선 사용, 없으면 자동 기록) |
| `memo` | String | ❌ | 단계별 메모 |

**[Response - data]** : null

---

### DELETE /api/applications/{id}/histories/{historyId}

**[비즈니스 로직]**

- Hard Delete
- 자동 생성된 '서류' 단계도 삭제 가능 (강제 보호 없음)
- 삭제 후 해당 historyId를 참조하는 `ALARM_LOG.application_history_id`는 null로 업데이트

**[Response - data]** : null

---

## 5. 알림 로그 (Alarm Log)

### 엔드포인트 목록

> 알림 발송은 Spring Scheduler 내부 처리 (`@Scheduled`)
> 이 API는 마이페이지 발송 이력 확인 전용

| 메서드   | 경로          | 설명                   | 인증 필요 |
| -------- | ------------- | ---------------------- | --------- |
| `GET`    | `/api/alarms` | 내 알림 발송 이력 조회 | ✅        |
| `DELETE` | `/api/alarms` | 알림 이력 선택 삭제    | ✅        |

**[연관 엔티티]** : `AlarmLog`, `Application`, `ApplicationHistory`

**[중복 발송 방지 정책]**

- DB 레벨에서 `UNIQUE KEY uq_alarm_prevent (user_id, application_id, alarm_type, DATE(sent_at))`로 원천 차단
- 스케줄러가 중복 실행되더라도 동일 날짜·동일 대상·동일 타입의 두 번째 INSERT는 유니크 제약 위반으로 실패
- 스케줄러 구현 시 `DuplicateKeyException`을 catch하여 정상 흐름으로 처리 (로그만 남기고 넘어감)

---

### GET /api/alarms

**[비즈니스 로직]**

- 최신순(`sent_at` DESC) 정렬 고정
- `label` 생성 규칙
    - `alarm_type = DEADLINE` : `application_history_id`가 null이므로 `'서류 마감'` 고정 반환
    - `alarm_type = D7, D3, D1` : 연결된 `APPLICATION_HISTORY.stage` 값 반환 (예: `'1차 면접'`)

**[Query Parameters]**
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `page` | int | ❌ | `0` | 페이지 번호 |
| `size` | int | ❌ | `20` | 페이지 크기 |

**[Response - data.content 단건]**
| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `id` | Long | ❌ | 알림 로그 PK |
| `companyName` | String | ❌ | 회사명 |
| `alarmType` | String | ❌ | `D7` \| `D3` \| `D1` \| `DEADLINE` |
| `label` | String | ❌ | 알림 대상 문구 (예: '1차 면접', '서류 마감') |
| `sentAt` | String | ❌ | 발송 시각 |
| `isSuccess` | Boolean | ❌ | 발송 성공 여부 |

---

### DELETE /api/alarms

**[비즈니스 로직]**

- 요청한 ids 중 인증된 유저 소유 항목만 삭제 (`user_id` 검증 포함)
- 타인 소유 id가 포함되어도 해당 항목만 무시 (소유권 검증은 쿼리 조건으로 처리)

**[Request Body]** : `List<Long>` (삭제할 알림 로그 id 목록)

**[Response - data]** : null

---

---

## 6. 분석 (Analytics) — Phase 2

### 엔드포인트 목록

| 메서드 | 경로 | 설명 | 인증 필요 |
|---|---|---|---|
| `GET` | `/api/analytics` | 분석 데이터 조회 | ✅ |

### GET /api/analytics

**[Query Parameters]**
| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `jobPosition` | String | ❌ | 직군 필터 (미입력 시 전체) |

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `trustIndicator.totalCount` | int | 전체 분석 대상 지원 수 |
| `trustIndicator.unstatedCount` | int | 히스토리 미입력 건수 |
| `trustIndicator.inProgressCount` | int | 진행중(결과 대기) 건수 |
| `funnel[].stage` | String | 전형 단계명 |
| `funnel[].entered` | int | 해당 단계 진입 수 |
| `funnel[].passed` | int | 통과 수 |
| `funnel[].failed` | int | 탈락 수 |
| `funnel[].passRate` | Double | 통과율 (결과 확정 건수 기준) |
| `durations[].fromStage` | String | 시작 단계 |
| `durations[].toStage` | String | 종료 단계 |
| `durations[].avgDays` | Double | 평균 소요 일수 |

---

## 7. 취준 여정 (Journey) — Phase 2

### 엔드포인트 목록

| 메서드 | 경로 | 설명 | 인증 필요 |
|---|---|---|---|
| `GET` | `/api/users/me/journey` | 취준 여정 조회 | ✅ |

### GET /api/users/me/journey

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `startDate` | String | 첫 지원일 |
| `endDate` | String | 최종 합격일 또는 현재 날짜 |
| `status` | String | `IN_PROGRESS` \| `PASSED` |
| `totalCount` | int | 총 지원 건수 |
| `passedCount` | int | 최종 합격 수 |
| `monthlyTimeline[]` | Array | 월별 지원/면접/합불 이벤트 |

---

## 8. 공지사항 (Notice) — Phase 2.5

### 엔드포인트 목록

| 메서드 | 경로 | 설명 | 인증 필요 |
|---|---|---|---|
| `GET` | `/api/notices/latest` | 최신 공지 1건 조회 | ✅ |
| `GET` | `/api/admin/notices` | 공지 목록 조회 (관리자) | ADMIN |
| `POST` | `/api/admin/notices` | 공지 생성 (관리자) | ADMIN |
| `PATCH` | `/api/admin/notices/{id}` | 공지 수정 (관리자) | ADMIN |
| `DELETE` | `/api/admin/notices/{id}` | 공지 삭제 (관리자) | ADMIN |

### GET /api/notices/latest

**[Response - data]**
| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `id` | Long | ❌ | 공지 PK |
| `title` | String | ❌ | 공지 제목 |
| `content` | String | ❌ | 공지 내용 |
| `createdAt` | String | ❌ | 작성 일시 |

공지가 없으면 `data: null` 반환.

---

## 9. 관리자 (Admin) — Phase 2.5

### 엔드포인트 목록

| 메서드 | 경로 | 설명 | 인증 필요 |
|---|---|---|---|
| `GET` | `/api/admin/stats` | 서비스 현황 통계 | ADMIN |
| `GET` | `/api/admin/users` | 사용자 목록 조회 | ADMIN |
| `DELETE` | `/api/admin/users/{id}` | 사용자 삭제 | ADMIN |

### GET /api/admin/stats

**[Response - data]**
| 필드 | 타입 | 설명 |
|---|---|---|
| `totalUsers` | int | 총 가입자 수 |
| `recentUsers[]` | Array | 최근 7일 일별 가입자 수 |
| `alarmStats.totalSent` | int | 총 알림 발송 수 |
| `alarmStats.failedCount` | int | 발송 실패 수 |
| `avgApplicationCount` | Double | 사용자 평균 지원 수 |
| `topCompanies[]` | Array | 가장 많이 지원된 회사 순위 (익명 집계) |

---

## 전체 API 목록 요약

```
[인증]
POST    /api/auth/kakao
POST    /api/auth/refresh
DELETE  /api/auth/logout

[유저]
GET     /api/users/me
PATCH   /api/users/me
DELETE  /api/users/me
POST    /api/users/me/profile-image
DELETE  /api/users/me/profile-image
GET     /api/users/me/journey

[지원 현황]
GET     /api/applications
POST    /api/applications
GET     /api/applications/{id}
PATCH   /api/applications/{id}
DELETE  /api/applications/{id}
GET     /api/applications/dashboard
GET     /api/applications/calendar

[전형 히스토리]
POST    /api/applications/{id}/histories
PATCH   /api/applications/{id}/histories/{historyId}
DELETE  /api/applications/{id}/histories/{historyId}

[알림 로그]
GET     /api/alarms
DELETE  /api/alarms

[분석]
GET     /api/analytics

[공지사항]
GET     /api/notices/latest
GET     /api/admin/notices
POST    /api/admin/notices
PATCH   /api/admin/notices/{id}
DELETE  /api/admin/notices/{id}

[관리자]
GET     /api/admin/stats
GET     /api/admin/users
DELETE  /api/admin/users/{id}
```
