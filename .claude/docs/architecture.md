# 아키텍처 설계 문서

## 개요

- **아키텍처** : Layered Architecture (경량 DDD) — Controller → Service → Repository
- **설계 철학** : 비즈니스 로직은 Entity에, Service(Application Service)는 흐름 조율만 담당
- **언어** : Java 21
- **프레임워크** : Spring Boot 3.4+
- **연관 문서** : `db-design.md`, `api-design.md`, `convention.md`

---

## 1. 패키지 구조

```
com.jobscope
├── domain
│   ├── user
│   │   ├── controller
│   │   │   └── UserController.java
│   │   ├── service
│   │   │   └── UserService.java
│   │   ├── repository
│   │   │   └── UserRepository.java
│   │   ├── entity
│   │   │   └── User.java
│   │   └── dto
│   │       ├── request
│   │       │   └── UpdateUserRequest.java
│   │       └── response
│   │           └── UserResponse.java
│   ├── application
│   │   ├── controller
│   │   │   ├── ApplicationController.java
│   │   │   └── ApplicationHistoryController.java
│   │   ├── service
│   │   │   ├── ApplicationService.java
│   │   │   └── ApplicationHistoryService.java
│   │   ├── repository
│   │   │   ├── ApplicationRepository.java
│   │   │   └── ApplicationHistoryRepository.java
│   │   ├── entity
│   │   │   ├── Application.java
│   │   │   └── ApplicationHistory.java
│   │   └── dto
│   │       ├── request
│   │       │   ├── CreateApplicationRequest.java
│   │       │   ├── UpdateApplicationRequest.java
│   │       │   ├── CreateHistoryRequest.java
│   │       │   └── UpdateHistoryRequest.java
│   │       └── response
│   │           ├── ApplicationSummaryResponse.java   (목록 조회용)
│   │           ├── ApplicationDetailResponse.java    (상세 조회용)
│   │           ├── DashboardResponse.java
│   │           └── CalendarResponse.java
│   ├── alarm
│   │   ├── controller
│   │   │   └── AlarmController.java
│   │   ├── service
│   │   │   └── AlarmService.java
│   │   ├── repository
│   │   │   └── AlarmLogRepository.java
│   │   ├── entity
│   │   │   └── AlarmLog.java
│   │   └── dto
│   │       └── response
│   │           └── AlarmLogResponse.java
│   ├── oauth
│   │   ├── service
│   │   │   └── OAuthTokenService.java   (카카오 토큰 갱신 전담)
│   │   ├── repository
│   │   │   └── OAuthTokenRepository.java
│   │   └── entity
│   │       └── OAuthToken.java
│   ├── analytics
│   │   ├── controller
│   │   │   └── AnalyticsController.java
│   │   ├── service
│   │   │   └── AnalyticsService.java
│   │   └── dto
│   │       └── response
│   │           └── AnalyticsResponse.java   (신뢰 지표 + 퍼널 + 소요 기간)
│   ├── notice
│   │   ├── controller
│   │   │   ├── NoticeController.java        (사용자용: GET /api/notices/latest)
│   │   │   └── AdminNoticeController.java   (관리자용: CRUD /api/admin/notices)
│   │   ├── service
│   │   │   └── NoticeService.java
│   │   ├── repository
│   │   │   └── NoticeRepository.java
│   │   ├── entity
│   │   │   └── Notice.java
│   │   └── dto
│   │       ├── request
│   │       │   ├── CreateNoticeRequest.java
│   │       │   └── UpdateNoticeRequest.java
│   │       └── response
│   │           └── NoticeResponse.java
│   └── admin
│       ├── controller
│       │   └── AdminController.java   (GET /api/admin/stats, GET/DELETE /api/admin/users)
│       ├── service
│       │   └── AdminService.java
│       └── dto
│           └── response
│               ├── AdminStatsResponse.java
│               └── AdminUserResponse.java
├── global
│   ├── config
│   │   ├── SecurityConfig.java
│   │   ├── JpaConfig.java
│   │   └── SwaggerConfig.java
│   ├── auth
│   │   ├── JwtProvider.java
│   │   ├── JwtFilter.java
│   │   └── KakaoAuthService.java
│   ├── alarm
│   │   └── KakaoMessageService.java     (카카오 나에게 보내기 발송)
│   ├── common
│   │   ├── response
│   │   │   ├── ApiResponse.java
│   │   │   ├── ErrorResponse.java       (별도 파일 분리, Inner Class 금지)
│   │   │   └── PageResponse.java
│   │   ├── exception
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BusinessException.java
│   │   │   └── ErrorCode.java
│   │   └── entity
│   │       ├── BaseEntity.java          (모든 엔티티 공통)
│   │       └── SoftDeleteEntity.java    (Soft Delete 엔티티 전용)
│   └── scheduler
│       └── AlarmScheduler.java
```

---

## 2. 레이어 역할과 의존성 방향

**의존성 방향 (단방향 엄수)**

```
Controller → Service → Repository
```

**도메인 간 호출 규칙**

```
같은 도메인 패키지 내부
→ Service에서 같은 도메인의 Repository 직접 참조 허용
→ 예) ApplicationService → ApplicationHistoryRepository 직접 참조 가능
   (Application과 ApplicationHistory는 같은 도메인)

다른 도메인 간 호출
→ Service에서 다른 도메인의 Repository 직접 참조 금지
→ 반드시 해당 도메인의 Service를 거쳐야 함
→ 예) ApplicationService → AlarmLogRepository 직접 참조 금지
       ApplicationService → AlarmService 경유 필수
```

**[Controller]**

```
- HTTP 요청/응답 처리만 담당
- 비즈니스 로직 작성 금지
- @Valid로 요청 값 검증
- Service 호출 후 ApiResponse로 감싸서 반환
- @AuthenticationPrincipal Long userId 로 인증된 유저 ID 접근
```

**[Service]**

```
- 흐름 조율 담당, 비즈니스 로직은 Entity에 위임
- HTTP 관련 코드 작성 금지 (HttpServletRequest 등)
- @Transactional 관리 (import 경로 주의 → 아래 @Transactional 규칙 참고)
- Repository 호출 및 Entity → DTO 변환
- 로그 기록 담당 (Controller, Repository에서는 로그 금지)
```

**[Repository]**

```
- DB 접근만 담당
- Spring Data JPA 인터페이스 사용
- 복잡한 쿼리는 @Query 사용
- Native Query 작성 시 주의사항 → 아래 @SQLRestriction 주의사항 참고
```

**[DTO]**

```
- Request DTO : Controller에서 요청 수신, @Valid 검증 어노테이션 포함
- Response DTO : Service에서 생성, Controller가 ApiResponse에 담아 반환
- Entity를 Response로 직접 반환 금지
- Inner Class 사용 금지 → 목적별 파일 분리 (예: SummaryResponse, DetailResponse)
```

**[Entity]**

```
- DB 테이블과 1:1 매핑
- 비즈니스 로직 메서드 포함 가능 (예: softDelete(), updateResult())
- Setter 금지, 의미있는 메서드명 사용
- 모든 엔티티는 BaseEntity 상속
- Soft Delete가 필요한 엔티티만 SoftDeleteEntity 상속 (현재 Application만 해당)
```

---

## 3. Lombok 사용 규칙

> Entity, DTO 모두 동일하게 적용

```
허용
@Getter                                             (Entity, DTO 모두)
@Builder                                            (Entity, DTO 모두)
@NoArgsConstructor(access = AccessLevel.PROTECTED)  (Entity)
@AllArgsConstructor(access = AccessLevel.PRIVATE)   (Entity)
@RequiredArgsConstructor                            (Service, Controller 생성자 주입용)
@Slf4j                                              (Service 로깅용)

금지
@Setter       → setter 대신 의미있는 메서드명 사용 (예: softDelete(), updateAlarmEnabled())
@Data         → @Setter 포함되어 있으므로 금지
@AllArgsConstructor (접근자 없이) → Entity에서 직접 생성자 호출 방지
```

**Entity 예시**

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Application extends SoftDeleteEntity {
    ...
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    public void updateAlarmEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }
}
```

**DTO 예시**

```java
@Getter
@Builder
public class ApplicationSummaryResponse {
    private Long id;
    private String companyName;
    ...

    public static ApplicationSummaryResponse from(Application application) {
        return ApplicationSummaryResponse.builder()
            .id(application.getId())
            .companyName(application.getCompanyName())
            ...
            .build();
    }
}
```

---

## 4. 공통 응답 형식

> 모든 API 응답은 ApiResponse<T>로 감싸서 반환
> ErrorResponse는 별도 파일로 분리 (Inner Class 금지 원칙 준수)

```java
// ApiResponse.java
@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    public static <T> ApiResponse<T> success(T data) { ... }
    public static ApiResponse<Void> success() { ... }       // data 없는 성공 응답
    public static ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode) {
        return ResponseEntity
            .status(errorCode.getStatus())                  // ErrorCode에서 상태코드 자동 참조
            .body(new ApiResponse<>(false, null,
                new ErrorResponse(errorCode.name(), errorCode.getMessage())));
    }
}

// ErrorResponse.java (별도 파일)
@Getter
public class ErrorResponse {
    private final String code;     // 예: "APPLICATION_NOT_FOUND"
    private final String message;  // 예: "지원 정보를 찾을 수 없습니다."
}
```

**Controller 반환 예시**

```java
// 성공 (data 있음) - GET, PATCH
return ResponseEntity.ok(ApiResponse.success(response));

// 성공 (data 있음) - POST
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));

// 성공 (data 없음) - DELETE
return ResponseEntity.ok(ApiResponse.success());

// 실패 - HTTP 상태코드 하드코딩 금지, ErrorCode에서 자동 참조
return ApiResponse.fail(ErrorCode.APPLICATION_NOT_FOUND);
```

---

## 5. 페이지네이션 전략

> Page<T> 방식 사용 (페이지 번호 기반 UI)
> ApiResponse<Page<T>> 직렬화 이슈 방지를 위해 PageResponse<T> 커스텀 래퍼 사용

```java
// PageResponse.java
@Getter
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .currentPage(page.getNumber())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .hasNext(page.hasNext())
            .build();
    }
}
```

**사용 예시**

```java
// Service
Page<Application> page = applicationRepository.findAll(pageable);
return PageResponse.from(page.map(ApplicationSummaryResponse::from));

// Controller
return ResponseEntity.ok(ApiResponse.success(pageResponse));
```

---

## 6. 예외처리 전략

**[구조]**

```
BusinessException (RuntimeException 상속)
└── 모든 커스텀 예외의 부모 클래스
    ErrorCode enum을 필드로 가짐

ErrorCode enum
└── 에러 코드, HTTP 상태, 메시지를 한 곳에서 관리

GlobalExceptionHandler (@RestControllerAdvice)
├── BusinessException              → ApiResponse.fail()로 변환
├── MethodArgumentNotValidException → INVALID_REQUEST 에러로 변환
└── Exception                      → 500 에러 반환 + ERROR 레벨 로그 기록
```

**[ErrorCode enum]**

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "만료된 토큰입니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),

    // 유저
    USER_NOT_FOUND(404, "유저를 찾을 수 없습니다."),

    // 지원 현황
    APPLICATION_NOT_FOUND(404, "지원 정보를 찾을 수 없습니다."),
    HISTORY_NOT_FOUND(404, "전형 단계를 찾을 수 없습니다."),

    // 공통
    INVALID_REQUEST(400, "잘못된 요청입니다."),

    // 외부 API
    KAKAO_AUTH_FAILED(502, "카카오 인증에 실패했습니다."),
    ALARM_SEND_FAILED(502, "카카오 메시지 발송에 실패했습니다.");

    private final int status;
    private final String message;
}
```

**[사용 예시]**

```java
// Service에서 예외 발생
if (!application.getUserId().equals(userId)) {
    throw new BusinessException(ErrorCode.FORBIDDEN);
}
```

---

## 7. 로깅 전략

**[로그 레벨 기준]**

```
INFO  → 정상 흐름 주요 이벤트
        예) 로그인 성공, 지원 등록, 알림 발송 성공
WARN  → 잠재적 문제 (서비스는 정상 동작)
        예) 알림 발송 실패, 중복 발송 시도 감지
ERROR → 즉시 확인 필요한 오류
        예) 외부 API 오류, 예상치 못한 예외
```

**[로그 위치]**

```
Service 레이어에서만 로그 기록
Controller, Repository 레이어에서는 로그 금지
```

**[형식]**

```java
// Lombok @Slf4j 어노테이션 사용
@Slf4j

log.info("[AlarmScheduler] 알림 발송 완료 - userId: {}, type: {}", userId, alarmType);
log.warn("[AlarmScheduler] 중복 발송 감지 - applicationId: {}", applicationId);
log.error("[KakaoAuthService] 카카오 API 오류 - {}", e.getMessage(), e);

// 형식: [클래스명] 설명 - 핵심 파라미터
```

---

## 8. Spring Security 필터 체인

**[인증 흐름]**

```
요청
└── JwtFilter (OncePerRequestFilter)
    ├── Authorization 헤더에서 토큰 추출
    ├── JwtProvider로 토큰 검증
    ├── 유효하면 SecurityContext에 Authentication 저장
    │   → UsernamePasswordAuthenticationToken의 principal에 userId(Long) 직접 저장
    │   → CustomUserDetails 클래스 만들지 않음
    └── 유효하지 않으면 다음 필터로 통과 (예외 던지지 않음)
        → 인증 필요한 API 접근 시 AuthenticationEntryPoint가 401 반환
```

**[JWT 만료 시간]**

```
Access Token  : 1시간
Refresh Token : 7일
→ JwtProvider에서 상수로 관리
```

**[Refresh Token 저장 전략]**

```
현재: DB 저장 (users 테이블 refresh_token, refresh_token_expires_at 컬럼)
추후: Redis 도입 시 Redis로 이전

로그아웃 시: refresh_token, refresh_token_expires_at 두 컬럼 모두 NULL 처리
재발급 시  : 두 컬럼 모두 새 값으로 업데이트
만료 검증  : refresh_token_expires_at > 현재 시각 비교
```

**[Controller에서 인증 유저 접근]**

```java
@GetMapping("/me")
public ResponseEntity<?> getMe(@AuthenticationPrincipal Long userId) { ... }
```

**[인증 불필요 경로 (permitAll)]**

```
POST /api/auth/kakao
POST /api/auth/refresh
GET  /actuator/health/**     ← 하위 경로 전체 허용 (/liveness, /readiness 포함)
```

**[인증 필요 경로]**

```
위 경로 외 모든 /api/** 경로
```

---

## 9. @Transactional 규칙

**[import 경로 — 반드시 준수]**

```java
// 반드시 이것만 사용 (Spring)
import org.springframework.transaction.annotation.Transactional;

// 금지 (jakarta — Spring 트랜잭션 전파·롤백 정책과 다를 수 있고, 프로젝트 표준 통일)
import jakarta.transaction.Transactional;
```

**[기본 원칙]**

```
@Transactional은 Service 레이어에서만 사용
조회 전용 메서드 → @Transactional(readOnly = true)
데이터 변경 메서드 → @Transactional
```

**[트랜잭션 묶음 기준]**

```
두 개 이상의 테이블을 동시에 변경하는 경우 반드시 하나의 트랜잭션으로 처리

예) 지원 등록
    APPLICATION INSERT + APPLICATION_HISTORY INSERT 동시 처리

예) result 변경
    APPLICATION.result UPDATE
    + APPLICATION_HISTORY(id 최댓값).stage_result UPDATE
    + APPLICATION_HISTORY(id 최댓값).completed_at UPDATE
    세 가지 동시 처리

예) 히스토리 stage_result 변경 (PASS/FAIL)
    APPLICATION_HISTORY.stage_result UPDATE
    + 해당 히스토리가 id 최댓값(최신 단계)인 경우
      APPLICATION.result도 동시 UPDATE
      PASS → APPLICATION.result = PASSED
      FAIL → APPLICATION.result = FAILED
```

**[AlarmScheduler 쿼리 필수 조건]**

```
알림 발송 대상 조회 시 아래 두 조건 반드시 포함
→ application.deleted_at IS NULL (Soft Delete된 지원건 제외)
→ application.alarm_enabled = TRUE (알림 OFF된 지원건 제외)
Native Query 사용 시 @SQLRestriction 자동 적용 안 되므로 직접 명시 필수
```

---

## 10. BaseEntity / SoftDeleteEntity

**[BaseEntity — 모든 엔티티 공통]**

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**[SoftDeleteEntity — Soft Delete 엔티티 전용]**

```java
@Getter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class SoftDeleteEntity extends BaseEntity {

    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
// 현재 SoftDeleteEntity를 상속하는 엔티티: Application만 해당
```

**[@SQLRestriction 적용 규칙]**

```
Hibernate 6에서 @MappedSuperclass에 선언한 @SQLRestriction은
하위 엔티티 클래스에 자동 상속되지 않음 (Hibernate 6 동작 변경)

→ SoftDeleteEntity에 @SQLRestriction이 선언되어 있더라도
  하위 엔티티(Application 등)에 반드시 직접 선언해야 함

// 필수 — Application.java
@Entity
@Table(name = "application")
@SQLRestriction("deleted_at IS NULL")  // 엔티티 클래스에 직접 선언
public class Application extends SoftDeleteEntity { ... }
```

**[@SQLRestriction Native Query 주의사항]**

```
@SQLRestriction("deleted_at IS NULL")은
JPQL과 Spring Data JPA 메서드에는 자동 적용됨

단, @Query(nativeQuery = true) 사용 시에는 적용되지 않음
→ Native Query 작성 시 WHERE deleted_at IS NULL 조건을 반드시 직접 추가해야 함

// 잘못된 예 (Soft Delete된 데이터가 조회될 수 있음)
@Query(value = "SELECT * FROM application WHERE user_id = :userId", nativeQuery = true)

// 올바른 예
@Query(value = "SELECT * FROM application WHERE user_id = :userId AND deleted_at IS NULL", nativeQuery = true)
```

**[JpaConfig — Auditing 활성화]**

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig { }
```
