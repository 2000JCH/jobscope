# DB 설계 문서

## 개요

- **DBMS** : MySQL 8.0 (Docker 컨테이너)
- **문자셋** : utf8mb4 (이모지 포함 한글 대응)
- **시간 기준** : KST 고정, 모든 DATETIME 컬럼은 KST 기준으로 저장
- **연관 문서** : `api-design.md`, `architecture.md`

---

## 테이블 관계 요약

```
USER 1 ─────────────── N APPLICATION
  │                          │
  │                          ├── 1 ─── N APPLICATION_HISTORY
  │                          │
  │                          └── 1 ─── N ALARM_LOG
  │                                          │
  │                                    N ───┘ APPLICATION_HISTORY
  │
  └── 1 ─── 1 OAUTH_TOKEN (provider=KAKAO)
```

- `USER` 삭제 시 → `APPLICATION`, `ALARM_LOG`, `OAUTH_TOKEN` cascade 삭제
- `APPLICATION` 삭제 시 → `APPLICATION_HISTORY` cascade 삭제 (단, Soft Delete이므로 실제 삭제는 아님)
- `APPLICATION_HISTORY` 삭제 시 → `ALARM_LOG.application_history_id` null 처리

---

## JPA 엔티티 공통 적용 규칙

> Claude가 엔티티 코드 작성 시 아래 규칙을 반드시 따를 것

```
1. 모든 엔티티에 @EntityListeners(AuditingEntityListener.class) 적용
   → created_at : @CreatedDate
   → updated_at : @LastModifiedDate

2. BaseEntity 추상 클래스로 created_at, updated_at 공통 관리
   → 모든 엔티티는 BaseEntity 상속
   → Soft Delete가 필요한 엔티티는 SoftDeleteEntity 상속 (현재 Application만 해당)

3. ENUM 타입은 @Enumerated(EnumType.STRING) 사용
   → EnumType.ORDINAL 절대 사용 금지 (순서 변경 시 데이터 깨짐)

4. Soft Delete 적용 엔티티(Application)는
   → SoftDeleteEntity 상속
   → @SQLRestriction("deleted_at IS NULL") 엔티티 클래스에 직접 선언 필수
     (Hibernate 6에서 @MappedSuperclass의 @SQLRestriction은 하위 클래스에 상속 안 됨)
   → deleted_at 컬럼 직접 수정 금지, 반드시 softDelete() 메서드로만 처리
   → Native Query 사용 시 WHERE deleted_at IS NULL 직접 추가 필수
     (@SQLRestriction은 JPQL에만 자동 적용, Native Query에는 미적용)

5. 연관관계 fetch 전략
   → @ManyToOne : FetchType.LAZY 기본 적용 (EAGER 사용 금지)
   → @OneToMany : FetchType.LAZY 기본 적용

6. cascade 전략
   → USER → APPLICATION : DB FK ON DELETE CASCADE로 처리 (JPA cascade 미사용)
   → APPLICATION → APPLICATION_HISTORY : CascadeType.ALL + orphanRemoval = true

7. 양방향 연관관계 금지
   → 단방향으로만 설계 (N 쪽에서 1 쪽을 참조)
   → @OneToMany 컬렉션 필드 선언 금지

8. Lombok 사용 규칙
   → @Getter 허용
   → @Setter 금지 (setter 대신 의미있는 메서드명 사용)
   → @Builder + @NoArgsConstructor(access = PROTECTED) + @AllArgsConstructor(access = PRIVATE)
   → @Data 금지 (@Setter 포함되어 있음)
```

---

## DDL

```sql
-- =============================================
-- USER: 서비스 사용자 (카카오 소셜 로그인 전용)
-- =============================================
-- [JPA 엔티티 힌트]
-- - 클래스명: User (@Table(name = "users") — MySQL 예약어 충돌 방지)
-- - 소셜 로그인 전용이므로 password 컬럼 없음
-- - kakao_id는 외부 식별자, id는 내부 식별자로 분리 운영
-- - refresh_token, refresh_token_expires_at: Refresh Token DB 저장 전략
--   로그아웃 시 두 컬럼 모두 NULL 처리
--   재발급 시 두 컬럼 모두 새 값으로 업데이트
--   만료 검증: refresh_token_expires_at > 현재 시각 비교
-- =============================================
CREATE TABLE users (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,   -- PK, 내부 식별자
    kakao_id                    VARCHAR(100)    NOT NULL,                  -- 카카오에서 발급한 고유 사용자 ID (변경 불가)
    nickname                    VARCHAR(50)     NOT NULL,                  -- 카카오 닉네임 (로그인 시 동기화, 직접 수정 가능)
    email                       VARCHAR(100),                              -- 카카오 이메일 (선택 동의 항목, NULL 가능)
    profile_image               VARCHAR(255),                              -- 카카오 프로필 이미지 URL (로그인 시 동기화)
    custom_profile_image        VARCHAR(500),                              -- S3 업로드 커스텀 이미지 URL (없으면 NULL → profile_image 사용)
    role                        ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER', -- 권한 (Phase 2.5 추가)
                                                                           -- USER: 일반 사용자, ADMIN: 관리자
                                                                           -- @Enumerated(EnumType.STRING) 필수
    last_login_at               DATETIME,                                  -- 마지막 로그인 시각 (Phase 2.5 추가, 카카오 로그인 시 갱신)
    refresh_token               VARCHAR(500),                              -- 발급된 Refresh Token (로그아웃 시 NULL)
    refresh_token_expires_at    DATETIME,                                  -- Refresh Token 만료 일시 (발급 시각 + 7일, 로그아웃 시 NULL)
    created_at                  DATETIME        NOT NULL,                  -- 최초 가입 시각 (@CreatedDate 자동 기록)
    updated_at                  DATETIME        NOT NULL,                  -- 마지막 정보 수정 시각 (@LastModifiedDate 자동 기록)
    PRIMARY KEY (id),
    UNIQUE KEY uq_kakao_id (kakao_id)                                      -- 동일 카카오 계정 중복 가입 방지
);


-- =============================================
-- OAUTH_TOKEN: 카카오 OAuth 토큰 저장
-- 나에게 보내기 API 발송 시 access_token 사용
-- =============================================
-- [JPA 엔티티 힌트]
-- - 클래스명: OAuthToken
-- - BaseEntity 상속
-- - (user_id, provider) UNIQUE → 동일 유저·동일 제공자 토큰은 1건만 유지
-- - 로그인 시 OAuthTokenService.saveOrUpdate()로 upsert 처리
-- - access_token 만료 시 OAuthTokenService.getValidAccessToken()이 refresh_token으로 갱신
--   → 갱신 후 access_token, access_token_expires_at, (갱신된 경우) refresh_token 업데이트
-- - Phase 1 보안 한계: 토큰 평문 저장 (Phase 2에서 암호화 예정)
-- - 유저 탈퇴(USER Hard Delete) 시 OAUTH_TOKEN도 cascade 삭제
-- =============================================
CREATE TABLE oauth_token (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                  BIGINT       NOT NULL,                  -- FK → users.id
    provider                 VARCHAR(20)  NOT NULL,                  -- 'KAKAO' (추후 확장 가능)
    access_token             VARCHAR(500),                           -- 카카오 access token (약 6시간 유효)
    access_token_expires_at  DATETIME,                               -- access token 만료 일시
    refresh_token            VARCHAR(500),                           -- 카카오 refresh token (약 2개월 유효)
    refresh_token_expires_at DATETIME,                               -- refresh token 만료 일시
    created_at               DATETIME     NOT NULL,                  -- @CreatedDate 자동 기록
    updated_at               DATETIME     NOT NULL,                  -- @LastModifiedDate 자동 기록
    PRIMARY KEY (id),
    UNIQUE KEY uq_oauth_token (user_id, provider),                  -- 동일 유저·동일 제공자 중복 방지
    CONSTRAINT fk_oauth_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE                                            -- 유저 탈퇴 시 토큰 cascade 삭제
);


-- =============================================
-- APPLICATION: 지원한 회사 1건 = 레코드 1건
-- =============================================
-- [JPA 엔티티 힌트]
-- - 클래스명: Application
-- - SoftDeleteEntity 상속 (deleted_at, softDelete() 메서드 포함)
-- - @SQLRestriction("deleted_at IS NULL") 엔티티 클래스에 직접 선언됨
--   (Hibernate 6에서 @MappedSuperclass 상속 미적용 → Application에 직접 추가)
-- - result 변경 시 반드시 서비스 레이어 트랜잭션으로
--   APPLICATION_HISTORY(id 최댓값)의 stage_result, completed_at 동시 업데이트
-- - USER와의 FK는 DB ON DELETE CASCADE로 처리 (JPA cascade 미사용)
-- - APPLICATION_HISTORY와는 CascadeType.ALL + orphanRemoval = true
--   단, @OneToMany 컬렉션 필드는 선언하지 않음 (단방향 유지)
-- =============================================
CREATE TABLE application (
    id              BIGINT          NOT NULL AUTO_INCREMENT,   -- PK
    user_id         BIGINT          NOT NULL,                  -- FK → users.id (소유 유저)
    company_name    VARCHAR(100)    NOT NULL,                  -- 지원 회사명
    job_position    VARCHAR(100)    NOT NULL,                  -- 지원 직군 (예: 백엔드, PM)
    applied_at      DATE            NOT NULL,                  -- 지원 날짜 (시각 불필요 → DATE, 미래 날짜 허용)
    deadline_at     DATETIME,                                  -- 서류 마감 일시 (NULL이면 마감 없음)
    result          ENUM('IN_PROGRESS','PASSED','FAILED')
                                    NOT NULL DEFAULT 'IN_PROGRESS', -- 최종 합불 결과
                                                               -- @Enumerated(EnumType.STRING) 필수
                                                               -- 변경 시 트랜잭션으로 APPLICATION_HISTORY 동시 업데이트
    alarm_enabled   BOOLEAN         NOT NULL DEFAULT TRUE,     -- 지원건별 알림 ON/OFF
                                                               -- TRUE: D-7, D-3, D-1, D-Day 전부 발송
                                                               -- FALSE: 해당 지원건 알림 전체 안 감
                                                               -- 스케줄러 쿼리 시 WHERE alarm_enabled = TRUE 조건 추가
    job_posting_url VARCHAR(500),                              -- 원본 채용 공고 URL (NULL 허용)
    memo            TEXT,                                      -- 자유 메모 (준비 사항 등, NULL 허용)
    retrospective   TEXT,                                      -- 탈락 회고 메모 (result=FAILED 시에만 저장, NULL 허용)
    deleted_at      DATETIME,                                  -- Soft Delete 기준값: NULL=정상, 값 있음=삭제됨
                                                               -- SoftDeleteEntity.softDelete()로만 처리
                                                               -- @SQLRestriction("deleted_at IS NULL") 자동 필터링
                                                               -- Native Query 사용 시 WHERE deleted_at IS NULL 직접 추가 필수
    created_at      DATETIME        NOT NULL,                  -- @CreatedDate 자동 기록
    updated_at      DATETIME        NOT NULL,                  -- @LastModifiedDate 자동 기록
    PRIMARY KEY (id),
    CONSTRAINT fk_application_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,                                     -- 유저 탈퇴 시 지원 데이터 cascade 삭제
    INDEX idx_application_user_id     (user_id),               -- 특정 유저의 지원 목록 조회 최적화
    INDEX idx_application_deadline_at (deadline_at),           -- 마감 임박 정렬 / 스케줄러 쿼리 최적화
    INDEX idx_application_result      (result),                -- 합불 필터링 최적화
    INDEX idx_application_alarm       (alarm_enabled)          -- 스케줄러 알림 대상자 조회 최적화
);


-- =============================================
-- APPLICATION_HISTORY: 전형 단계별 진행 기록
-- 1건의 APPLICATION이 여러 단계를 거침
-- 예) 서류(PENDING) → 서류(PASS) → 코테(PENDING) → 1차 면접(PENDING)
-- =============================================
-- [JPA 엔티티 힌트]
-- - 클래스명: ApplicationHistory
-- - BaseEntity 상속 (Soft Delete 없음)
-- - '최신 히스토리' 기준은 id 최댓값 (created_at 기준 사용 금지)
--   → 유저가 단계를 순서와 다르게 등록할 수 있어 created_at이 꼬일 수 있음
-- - stage_result 변경 시 completed_at 처리 우선순위
--   1순위: 요청에 completedAt 값이 있으면 그 값 사용
--   2순위: completedAt 없고 stageResult가 PASS/FAIL로 변경되면 현재 시각(KST) 자동 기록
--   3순위: stageResult 변경 없으면 completed_at 그대로 유지
-- - APPLICATION Soft Delete 시 이 테이블은 Hard Delete 하지 않음
--   → @SQLRestriction이 Application에 걸려 있으므로 조회에서 자동 제외됨
-- - 지원 등록(POST /api/applications) 시 stage='서류', stage_result='PENDING' 자동 생성
--   → APPLICATION INSERT와 동일 트랜잭션으로 처리
-- =============================================
CREATE TABLE application_history (
    id              BIGINT          NOT NULL AUTO_INCREMENT,   -- PK, '최신 히스토리' 판별 기준값
    application_id  BIGINT          NOT NULL,                  -- FK → application.id
    stage           VARCHAR(100)    NOT NULL,                  -- 전형 단계명 (예: '서류', '코딩테스트', '1차 면접')
                                                               -- 자유 입력값, ENUM 아님 (유저 정의 단계 허용)
    stage_result    ENUM('PENDING','PASS','FAIL')
                                    NOT NULL DEFAULT 'PENDING', -- 해당 단계 결과
                                                               -- PENDING: 예정/진행중, PASS: 통과, FAIL: 탈락
                                                               -- @Enumerated(EnumType.STRING) 필수
    scheduled_at    DATETIME,                                  -- 단계 예정 일시 (면접·코테 일정, 알림 트리거 기준)
    completed_at    DATETIME,                                  -- 단계 실제 완료 일시 (위 처리 우선순위 참고)
    memo            TEXT,                                      -- 단계별 메모 (면접 질문, 준비 내용 등, NULL 허용)
    created_at      DATETIME        NOT NULL,                  -- @CreatedDate 자동 기록
    updated_at      DATETIME        NOT NULL,                  -- @LastModifiedDate 자동 기록
    PRIMARY KEY (id),
    CONSTRAINT fk_history_application
        FOREIGN KEY (application_id) REFERENCES application(id)
        ON DELETE CASCADE,                                     -- APPLICATION Hard Delete 시 cascade (탈퇴 시나리오)
    INDEX idx_history_application_id (application_id),         -- 특정 지원건의 전형 단계 조회 최적화
    INDEX idx_history_scheduled_at   (scheduled_at)            -- 캘린더·스케줄러 날짜 범위 조회 최적화
);


-- =============================================
-- ALARM_LOG: 카카오 나에게 보내기 발송 이력
-- 중복 발송 방지 및 발송 성공/실패 추적용
-- =============================================
-- [JPA 엔티티 힌트]
-- - 클래스명: AlarmLog
-- - BaseEntity 상속
-- - INSERT 전용 테이블 (수정/삭제 없음, 이력 보존 목적)
-- - Spring Scheduler가 내부적으로 INSERT, 클라이언트 직접 호출 없음
-- - 발송 방식: 카카오 "나에게 보내기" API (oauth_token.access_token 사용)
-- - 중복 발송 방지는 uq_alarm_prevent 유니크 키로 DB 레벨에서 원천 차단
--   → 스케줄러에서 DuplicateKeyException catch 후 로그만 남기고 정상 흐름 처리
-- - application_history_id nullable 처리 이유
--   → DEADLINE 타입: 서류 마감 알림이므로 히스토리 무관 → NULL
--   → D7/D3/D1 타입: 특정 면접/코테 히스토리 기준 → 히스토리 id 저장
-- - APPLICATION_HISTORY 삭제 시 application_history_id → NULL 처리 (ON DELETE SET NULL)
-- - 유저 탈퇴(USER Hard Delete) 시 ALARM_LOG도 cascade 삭제
-- =============================================
CREATE TABLE alarm_log (
    id                      BIGINT      NOT NULL AUTO_INCREMENT,  -- PK
    user_id                 BIGINT      NOT NULL,                 -- FK → users.id (수신 유저)
    application_id          BIGINT      NOT NULL,                 -- FK → application.id (대상 지원건)
    application_history_id  BIGINT,                              -- FK → application_history.id
                                                                  -- NULL: DEADLINE 알림 (서류 마감, 히스토리 무관)
                                                                  -- NOT NULL: D7/D3/D1 알림 (특정 면접/코테 기준)
    alarm_type              ENUM('D7','D3','D1','DEADLINE') NOT NULL, -- D7: D-7일 전, D3: D-3일 전
                                                                      -- D1: D-1일 전, DEADLINE: 마감 당일
                                                                      -- @Enumerated(EnumType.STRING) 필수
    sent_at                 DATETIME    NOT NULL,                 -- 실제 알림톡 발송 시각 (KST, 매일 오전 9시)
    is_success              BOOLEAN     NOT NULL,                 -- 발송 성공 여부 (카카오 API 응답 기준)
    PRIMARY KEY (id),
    CONSTRAINT fk_alarm_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,                                        -- 유저 탈퇴 시 알림 이력 cascade 삭제
    CONSTRAINT fk_alarm_application
        FOREIGN KEY (application_id) REFERENCES application(id)
        ON DELETE CASCADE,                                        -- 지원 Hard Delete 시 cascade 삭제 (탈퇴 시나리오)
    CONSTRAINT fk_alarm_history
        FOREIGN KEY (application_history_id) REFERENCES application_history(id)
        ON DELETE SET NULL,                                       -- 히스토리 삭제 시 NULL 처리 (이력은 보존)
    INDEX idx_alarm_user_id    (user_id),                        -- 특정 유저 알림 이력 조회
    INDEX idx_alarm_sent_at    (sent_at),                        -- 날짜 범위 기반 쿼리 최적화
    INDEX idx_alarm_history_id (application_history_id),         -- 특정 전형 단계 알림 이력 조회

    -- 동일 날짜·동일 유저·동일 지원건·동일 알림 타입 중복 발송 방지 (DB 레벨 원천 차단)
    -- MySQL 8.0.13+ 함수 기반 인덱스 사용
    -- MySQL 8.0.13 미만이라면 sent_date DATE 컬럼 별도 추가 후
    -- UNIQUE KEY uq_alarm_prevent (user_id, application_id, alarm_type, sent_date) 로 대체
    UNIQUE KEY uq_alarm_prevent (user_id, application_id, alarm_type, (DATE(sent_at)))
);


-- =============================================
-- NOTICE: 공지사항 (Phase 2.5 추가)
-- 관리자가 작성, 사용자 MY 페이지 벨 아이콘으로 최신 1건 조회
-- =============================================
-- [JPA 엔티티 힌트]
-- - 클래스명: Notice
-- - BaseEntity 상속
-- - active=false이면 비활성화 (조회에서 제외)
-- - 사용자 API: GET /api/notices/latest → active=true인 최신 1건 반환
-- - 관리자 API: CRUD 전체 (active 포함 모든 건 조회)
-- =============================================
CREATE TABLE notice (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    title      VARCHAR(200) NOT NULL,                  -- 공지 제목
    content    TEXT         NOT NULL,                  -- 공지 내용 (마크다운 아님, plain text)
    active     BOOLEAN      NOT NULL DEFAULT TRUE,     -- 활성화 여부 (false이면 사용자에게 노출 안 됨)
    created_at DATETIME     NOT NULL,                  -- @CreatedDate 자동 기록
    updated_at DATETIME     NOT NULL,                  -- @LastModifiedDate 자동 기록
    PRIMARY KEY (id),
    INDEX idx_notice_active (active)                   -- 활성 공지 필터링 최적화
);


-- =============================================
-- [추후 확장] 그룹 스터디 모드 (Phase 3)
-- =============================================
-- Phase 3 구현 시 아래 작업 필요:
-- 1. application 테이블에 컬럼 추가
--    ALTER TABLE application ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT FALSE;
--    → TRUE이면 소속 그룹 내 공개

-- 2. 아래 테이블 추가
-- CREATE TABLE group_study (
--     id          BIGINT       NOT NULL AUTO_INCREMENT,
--     name        VARCHAR(100) NOT NULL,               -- 그룹명
--     invite_code VARCHAR(20)  NOT NULL,               -- 초대 코드 (랜덤 발급)
--     created_by  BIGINT       NOT NULL,               -- 그룹 생성자 FK → users.id
--     created_at  DATETIME     NOT NULL,
--     PRIMARY KEY (id),
--     UNIQUE KEY uq_invite_code (invite_code)
-- );

-- CREATE TABLE group_member (
--     id          BIGINT   NOT NULL AUTO_INCREMENT,
--     group_id    BIGINT   NOT NULL,                   -- FK → group_study.id
--     user_id     BIGINT   NOT NULL,                   -- FK → users.id
--     joined_at   DATETIME NOT NULL,
--     PRIMARY KEY (id),
--     CONSTRAINT fk_gm_group FOREIGN KEY (group_id) REFERENCES group_study(id) ON DELETE CASCADE,
--     CONSTRAINT fk_gm_user  FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
--     UNIQUE KEY uq_group_member (group_id, user_id)  -- 동일 유저 중복 가입 방지
-- );
```

---

## 삭제 정책 요약

| 트리거 | 대상 | 처리 방식 |
|---|---|---|
| 유저 탈퇴 (`DELETE /api/users/me`) | `application` | `ON DELETE CASCADE` |
| 유저 탈퇴 (`DELETE /api/users/me`) | `alarm_log` | `ON DELETE CASCADE` |
| 유저 탈퇴 (`DELETE /api/users/me`) | `oauth_token` | `ON DELETE CASCADE` |
| 지원 삭제 (`DELETE /api/applications/{id}`) | `application.deleted_at` | Soft Delete |
| 지원 삭제 (Soft Delete) | `application_history` | 삭제 안 함, 조회에서만 제외 |
| 지원 삭제 (Soft Delete) | `alarm_log` | 삭제 안 함, 이력 보존 |
| 유저 탈퇴 시 Hard Delete cascade | `application_history` | `ON DELETE CASCADE` |
| 히스토리 삭제 (`DELETE /api/.../histories/{id}`) | `alarm_log.application_history_id` | `ON DELETE SET NULL` |
| 알림 이력 수동 삭제 (`DELETE /api/alarms`) | `alarm_log` (선택 건) | Hard Delete (userId 소유 검증 후) |

---

## 인덱스 전략 요약

| 테이블 | 인덱스 | 용도 |
|---|---|---|
| `application` | `idx_application_user_id` | 특정 유저 지원 목록 조회 |
| `application` | `idx_application_deadline_at` | 마감 임박 정렬, 스케줄러 쿼리 |
| `application` | `idx_application_result` | 합불 필터링 |
| `application` | `idx_application_alarm` | 스케줄러 알림 대상자 조회 |
| `application_history` | `idx_history_application_id` | 특정 지원건 전형 단계 조회 |
| `application_history` | `idx_history_scheduled_at` | 캘린더, 스케줄러 날짜 범위 조회 |
| `alarm_log` | `idx_alarm_user_id` | 특정 유저 알림 이력 조회 |
| `alarm_log` | `idx_alarm_sent_at` | 날짜 범위 쿼리 |
| `alarm_log` | `idx_alarm_history_id` | 특정 히스토리 알림 이력 조회 |
| `alarm_log` | `uq_alarm_prevent` | 중복 발송 원천 차단 |
| `oauth_token` | `uq_oauth_token` | 동일 유저·동일 제공자 중복 방지 |
| `notice` | `idx_notice_active` | 활성 공지 필터링 최적화 |
