# 코드 컨벤션 문서

## 개요

- **언어** : Java 21
- **빌드 도구** : Gradle
- **연관 문서** : `architecture.md`

---

## 1. 네이밍 규칙

### 클래스명
```
Controller  : {Domain}Controller
             예) UserController, ApplicationController

Service     : {Domain}Service
             예) UserService, ApplicationService

Repository  : {Domain}Repository
             예) UserRepository, ApplicationRepository

Entity      : {Domain} (단수형)
             예) User, Application, ApplicationHistory

Request DTO : {동사}{Domain}Request
             예) CreateApplicationRequest, UpdateUserRequest

Response DTO: {Domain}{목적}Response
             예) ApplicationSummaryResponse, ApplicationDetailResponse
             예) DashboardResponse, CalendarResponse

Enum        : {Domain}{목적} (값은 UPPER_SNAKE_CASE)
             예) AlarmType.D7, AlarmType.DEADLINE
```

### 메서드명
```
조회 (단건)  : find{Domain}By{조건}
              예) findApplicationById()

조회 (목록)  : find{Domain}s
              예) findApplications()

등록         : create{Domain}
              예) createApplication()

수정         : update{Domain}
              예) updateApplication()

삭제         : delete{Domain}
              예) deleteApplication()

검증         : validate{대상}
              예) validateApplicationOwner()

Entity 내부  : 의미있는 동사 사용 (get/set 금지)
              예) softDelete(), updateResult(), updateAlarmEnabled()
```

### 변수명
```
- camelCase 사용
- 약어 사용 금지 (예: userId O, usrId X)
- Boolean 변수는 is{상태} 형태
  예) isSuccess, alarmEnabled
- 컬렉션은 복수형
  예) applications, histories
```

### 상수명
```
- UPPER_SNAKE_CASE 사용
- 예) ACCESS_TOKEN_EXPIRE_TIME, REFRESH_TOKEN_EXPIRE_TIME
```

---

## 2. REST URL 네이밍 규칙

```
리소스는 복수형 명사 사용
예) /api/applications  (O)
    /api/application   (X)

케밥케이스 사용
예) /api/alarm-logs    (O)
    /api/alarmLogs     (X)

계층 구조는 URL로 표현
예) /api/applications/{applicationId}/histories

동사 사용 금지 (행위는 HTTP 메서드로 표현)
예) /api/applications/{id}/delete  (X)
    DELETE /api/applications/{id}  (O)
```

---

## 3. HTTP 응답 상태코드 규칙

```
GET    → 200 OK
POST   → 201 Created
PATCH  → 200 OK
DELETE → 200 OK  (204 아님 — ApiResponse 형식 유지를 위해 200으로 통일)
```

---

## 4. 패키지 및 파일 규칙

```
패키지명     : 소문자, 단수형
              예) domain.user, domain.application

파일당 클래스: 1파일 1클래스 원칙
              Inner Class 금지 (architecture.md 참고)

임포트 순서  : IntelliJ 기본 설정 따름
              (Java → 외부 라이브러리 → 프로젝트 내부)
              와일드카드 임포트 금지 (import com.example.*)
```

---

## 5. 어노테이션 순서

```java
// Entity
@Entity
@Table(name = "...")
@SQLRestriction("...")   // Soft Delete 엔티티만, @Table 바로 아래 배치
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Application extends SoftDeleteEntity { }

// DTO
@Getter
@Builder
public class ApplicationSummaryResponse { }

// Controller
@RestController
@RequestMapping("/api/...")
@RequiredArgsConstructor
public class ApplicationController { }

// Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // 클래스 레벨 기본값
@Slf4j
public class ApplicationService { }
```

---

## 6. API 응답 규칙

```java
// GET, PATCH → 200
return ResponseEntity.ok(ApiResponse.success(response));

// POST → 201
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));

// DELETE → 200
return ResponseEntity.ok(ApiResponse.success());

// 실패 → ErrorCode에서 상태코드 자동 참조 (하드코딩 금지)
return ApiResponse.fail(ErrorCode.APPLICATION_NOT_FOUND);

// 페이지네이션
return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
```

---

## 7. Git 브랜치 전략

```
main
└── 배포 가능한 상태만 유지
    직접 push 금지, PR로만 merge

develop
└── 개발 통합 브랜치
    feat 브랜치 merge 대상 → develop에서 main으로 PR

feat/{domain}-backend
feat/{domain}-frontend
└── 도메인별 기능 브랜치 → develop으로 PR
    예) feat/user-backend
    예) feat/user-frontend
    예) feat/application-backend

hotfix/{이슈명}
└── 운영 긴급 수정
    예) hotfix/alarm-duplicate-fix
```

---

## 8. 커밋 메시지 규칙

```
형식: {타입}: {내용} (#{이슈번호})

타입
feat     : 새 기능 추가
fix      : 버그 수정
refactor : 코드 리팩토링 (기능 변경 없음)
docs     : 문서 수정
test     : 테스트 코드
chore    : 빌드, 설정 변경
style    : 포맷, 세미콜론 등 (로직 변경 없음)

예시
feat: 카카오 소셜 로그인 구현 (#1)
fix: 알림 중복 발송 방지 로직 수정 (#12)
refactor: ApplicationService 트랜잭션 분리 (#8)
docs: API 설계 문서 업데이트
chore: Docker Compose 환경변수 설정 추가
```

---

## 9. 테스트 규칙

```
테스트 파일명 : {클래스명}Test.java
테스트 메서드 : {메서드명}_{시나리오}_{예상결과}
               예) createApplication_success()
               예) createApplication_whenUserNotFound_throwsException()

단위 테스트   : Service 레이어 중심
               Mockito로 Repository mocking
통합 테스트   : Controller 레이어
               @SpringBootTest + MockMvc 사용
```

**테스트 내부 구조 — Given-When-Then 강제**
```java
@Test
@DisplayName("지원 등록 성공")  // 한국어로 작성
void createApplication_success() {
    // given
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    ApplicationDetailResponse result = applicationService.createApplication(userId, request);

    // then
    assertThat(result.getCompanyName()).isEqualTo("네이버");  // AssertJ 사용
}

// JUnit5 기본 assert 금지
// assertEquals(expected, actual)  (X)
// assertThat(actual).isEqualTo(expected)  (O)
```

---

## 10. 환경변수 규칙

```
민감 정보는 절대 코드에 하드코딩 금지
application.yml에 직접 작성 금지

관리 방식
└── .env 파일로 관리 (Git 추적 제외, .gitignore에 추가)
└── application.yml에서 ${ENV_VAR} 형태로 참조

예시
kakao:
  client-id: ${KAKAO_CLIENT_ID}
  client-secret: ${KAKAO_CLIENT_SECRET}

jwt:
  secret: ${JWT_SECRET}
  access-token-expire: ${JWT_ACCESS_EXPIRE}    # 단위: ms (3600000 = 1시간)
  refresh-token-expire: ${JWT_REFRESH_EXPIRE}  # 단위: ms (604800000 = 7일)
```

---

## 11. 주석 규칙

```
코드로 의도가 명확한 경우 주석 금지
주석이 필요한 경우 → 코드를 더 명확하게 리팩토링 먼저 시도

허용하는 주석
JavaDoc      → public 메서드에 작성 (Service 레이어 기준)
               @param, @return, @throws 포함
// TODO      → 나중에 구현할 내용
// FIXME     → 알고 있는 버그, 임시 처리
// (인라인)  → 복잡한 알고리즘·조건 분기에만 허용, 자명한 코드에는 금지
// NOTE      → 중요한 비즈니스 규칙 설명
               예) // NOTE: id 최댓값 기준으로 최신 히스토리 판별 (created_at 사용 금지)
```

```java
/**
 * 지원서를 생성하고 초기 히스토리를 등록한다.
 *
 * @param userId  사용자 ID
 * @param request 지원서 생성 요청 DTO
 * @return 생성된 지원서 상세 응답
 * @throws BusinessException 동일 공고에 중복 지원 시 (DUPLICATE_APPLICATION)
 */
@Transactional
public ApplicationDetailResponse createApplication(Long userId, ApplicationCreateRequest request) {

    // NOTE: id 최댓값 기준으로 최신 히스토리 판별 (created_at 사용 금지)
    ApplicationHistory latest = historyRepository
        .findTopByApplicationIdOrderByIdDesc(applicationId)
        .orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_NOT_FOUND));

    // TODO: 알림 스케줄 등록 로직 추가 필요
}
```

---

## 12. 로깅 규칙

```
- 로그는 Service 레이어에서만 작성 — Controller, Repository 금지
- @Slf4j 사용
- 형식: [클래스명] 설명 - 핵심 파라미터
```

```java
log.info("[AlarmScheduler] 알림 발송 완료 - userId: {}, type: {}", userId, alarmType);
log.warn("[AlarmScheduler] 중복 발송 감지 - applicationId: {}", applicationId);
log.error("[KakaoAuthService] 카카오 API 오류 - {}", e.getMessage(), e);
```

```
info  → 정상 흐름 주요 이벤트
warn  → 중복 발송 등 예외적이지만 처리 가능한 상황
error → 외부 API 실패 등 시스템 오류
```
