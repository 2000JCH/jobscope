package com.jobscope.domain.application.service;

import com.jobscope.domain.application.dto.request.CreateApplicationRequest;
import com.jobscope.domain.application.dto.request.UpdateApplicationRequest;
import com.jobscope.domain.application.dto.response.*;
import com.jobscope.domain.application.entity.*;
import com.jobscope.domain.application.repository.ApplicationHistoryRepository;
import com.jobscope.domain.application.repository.ApplicationRepository;
import com.jobscope.global.common.exception.BusinessException;
import com.jobscope.global.common.exception.ErrorCode;
import com.jobscope.global.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private Application findAndValidate(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
        application.validateApplicationOwner(userId);
        return application;
    }

    /**
     * 지원 목록을 조회한다. 검색어, 결과 필터, 정렬을 지원하며 페이지네이션으로 반환한다.
     *
     * @param userId  인증된 사용자 ID
     * @param search  회사명 검색어 (null이면 전체)
     * @param results 결과 필터 목록 (null 또는 빈 리스트면 전체)
     * @param sort    정렬 기준 (null이면 LATEST)
     * @param page    페이지 번호
     * @param size    페이지 크기
     * @return 지원 목록 페이지 응답
     */
    public PageResponse<ApplicationSummaryResponse> findApplications(
            Long userId, String search, List<ApplicationResult> results,
            ApplicationSort sort, int page, int size) {

        Sort sortOrder = switch (sort != null ? sort : ApplicationSort.LATEST) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case DEADLINE -> Sort.by(Sort.Order.asc("deadlineAt").nullsLast());
            case UPDATED -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        LocalDate today = LocalDate.now(KST);

        Page<Application> applicationPage;
        boolean resultFilter = results != null && !results.isEmpty();
        if (resultFilter) {
            applicationPage = applicationRepository.findByUserIdWithSearchAndResults(userId, search, results, pageable);
        } else {
            applicationPage = applicationRepository.findByUserIdWithSearch(userId, search, pageable);
        }

        // NOTE: N+1 방지 — applicationId 목록으로 최신 히스토리 배치 조회
        List<Long> applicationIds = applicationPage.getContent().stream()
                .map(Application::getId).toList();
        Map<Long, String> latestStageMap = applicationHistoryRepository
                .findLatestByApplicationIds(applicationIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        h -> h.getApplication().getId(),
                        ApplicationHistory::getStage));

        return PageResponse.from(applicationPage.map(app -> {
            String currentStage = latestStageMap.getOrDefault(app.getId(), "서류");
            Long dDay = app.calculateDDay(today);
            return ApplicationSummaryResponse.of(app, currentStage, dDay);
        }));
    }

    /**
     * 지원을 등록한다. APPLICATION INSERT와 서류 단계 HISTORY INSERT를 동일 트랜잭션으로 처리한다.
     *
     * @param userId  인증된 사용자 ID
     * @param request 지원 등록 요청 DTO
     * @return 생성된 Application PK
     */
    @Transactional
    public Long createApplication(Long userId, CreateApplicationRequest request) {
        Application application = Application.builder()
                .userId(userId)
                .companyName(request.getCompanyName())
                .jobPosition(request.getJobPosition())
                .appliedAt(request.getAppliedAt())
                .deadlineAt(request.getDeadlineAt())
                .alarmEnabled(request.getAlarmEnabled() != null ? request.getAlarmEnabled() : true)
                .jobPostingUrl(request.getJobPostingUrl())
                .memo(request.getMemo())
                .build();
        applicationRepository.save(application);

        ApplicationHistory initialHistory = ApplicationHistory.createInitial(application);
        applicationHistoryRepository.save(initialHistory);

        log.info("[ApplicationService] 지원 등록 완료 - userId: {}, applicationId: {}", userId, application.getId());
        return application.getId();
    }

    /**
     * 지원 상세를 조회한다. 전형 단계 히스토리를 id 오름차순으로 함께 반환한다.
     *
     * @param id     Application PK
     * @param userId 인증된 사용자 ID
     * @return 지원 상세 응답 (histories 포함)
     * @throws BusinessException 지원 없음(APPLICATION_NOT_FOUND) 또는 타인 리소스(FORBIDDEN)
     */
    public ApplicationDetailResponse findApplicationDetail(Long id, Long userId) {
        Application application = findAndValidate(id, userId);
        LocalDate today = LocalDate.now(KST);
        Long dDay = application.calculateDDay(today);

        List<ApplicationHistoryResponse> histories = applicationHistoryRepository
                .findByApplication_IdOrderByIdAsc(id)
                .stream()
                .map(ApplicationHistoryResponse::from)
                .toList();

        return ApplicationDetailResponse.of(application, dDay, histories);
    }

    /**
     * 지원을 수정한다. null 필드는 변경하지 않는다.
     * result를 PASSED/FAILED로 변경 시 최신 히스토리의 stageResult·completedAt을 동시 업데이트한다.
     *
     * @param id      Application PK
     * @param userId  인증된 사용자 ID
     * @param request 수정 요청 DTO
     * @throws BusinessException 지원 없음(APPLICATION_NOT_FOUND) 또는 타인 리소스(FORBIDDEN)
     */
    @Transactional
    public void updateApplication(Long id, Long userId, UpdateApplicationRequest request) {
        Application application = findAndValidate(id, userId);

        application.updateBasicInfo(
                request.getCompanyName(), request.getJobPosition(), request.getAppliedAt(),
                request.getDeadlineAt(), request.getJobPostingUrl(), request.getMemo());

        if (request.getAlarmEnabled() != null) {
            application.updateAlarmEnabled(request.getAlarmEnabled());
        }

        if (request.getResult() != null) {
            application.updateResult(request.getResult());
            if (request.getResult() == ApplicationResult.PASSED || request.getResult() == ApplicationResult.FAILED) {
                StageResult stageResult = request.getResult() == ApplicationResult.PASSED
                        ? StageResult.PASS : StageResult.FAIL;
                // NOTE: id 최댓값 기준으로 최신 히스토리 판별 (created_at 사용 금지)
                applicationHistoryRepository.findTopByApplication_IdOrderByIdDesc(id)
                        .ifPresent(history -> history.updateStageResultWithCompletion(
                                stageResult, LocalDateTime.now(KST)));
            }
        }

        if (request.getRetrospective() != null) {
            application.updateRetrospective(request.getRetrospective());
        }

        log.info("[ApplicationService] 지원 수정 완료 - userId: {}, applicationId: {}", userId, id);
    }

    /**
     * 지원을 삭제한다 (Soft Delete). HISTORY는 Hard Delete 하지 않으며 조회에서만 제외된다.
     *
     * @param id     Application PK
     * @param userId 인증된 사용자 ID
     * @throws BusinessException 지원 없음(APPLICATION_NOT_FOUND) 또는 타인 리소스(FORBIDDEN)
     */
    @Transactional
    public void deleteApplication(Long id, Long userId) {
        Application application = findAndValidate(id, userId);
        application.softDelete();
        log.info("[ApplicationService] 지원 삭제 완료 - userId: {}, applicationId: {}", userId, id);
    }

    /**
     * 대시보드 데이터를 조회한다.
     * 이번 주(월~일) 면접 일정과 D-3 이내 마감 임박 지원을 포함한다.
     *
     * @param userId 인증된 사용자 ID
     * @return 요약 카운트, 이번 주 일정, 마감 임박 목록
     */
    public DashboardResponse getDashboard(Long userId) {
        long total = applicationRepository.countByUserId(userId);
        long inProgress = applicationRepository.countByUserIdAndResult(userId, ApplicationResult.IN_PROGRESS);
        long passed = applicationRepository.countByUserIdAndResult(userId, ApplicationResult.PASSED);
        long failed = applicationRepository.countByUserIdAndResult(userId, ApplicationResult.FAILED);

        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .total(total).inProgress(inProgress).passed(passed).failed(failed)
                .build();

        LocalDate today = LocalDate.now(KST);
        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(23, 59, 59);

        List<DashboardScheduleResponse> thisWeekSchedules = applicationHistoryRepository
                .findByUserIdAndScheduledAtBetween(userId, weekStart, weekEnd)
                .stream()
                .map(h -> DashboardScheduleResponse.builder()
                        .applicationId(h.getApplication().getId())
                        .companyName(h.getApplication().getCompanyName())
                        .stage(h.getStage())
                        .scheduledAt(h.getScheduledAt())
                        .build())
                .toList();

        // NOTE: D-3 이내는 날짜 기준 — 시각 기준 72시간이 아닌 오늘~3일 뒤 자정까지
        LocalDateTime deadlineStart = today.atStartOfDay();
        LocalDateTime deadlineEnd = today.plusDays(3).atTime(23, 59, 59);

        List<DashboardDeadlineResponse> imminentDeadlines = applicationRepository
                .findImminentDeadlines(userId, deadlineStart, deadlineEnd)
                .stream()
                .map(a -> DashboardDeadlineResponse.builder()
                        .applicationId(a.getId())
                        .companyName(a.getCompanyName())
                        .deadlineAt(a.getDeadlineAt())
                        .dDay(a.calculateDDay(today))
                        .build())
                .toList();

        return DashboardResponse.builder()
                .summary(summary)
                .thisWeekSchedules(thisWeekSchedules)
                .imminentDeadlines(imminentDeadlines)
                .build();
    }

    /**
     * 캘린더용 일정을 조회한다. INTERVIEW(scheduled_at)와 DEADLINE(deadline_at) 이벤트를 날짜별로 그룹핑해 반환한다.
     *
     * @param userId    인증된 사용자 ID
     * @param startDate 조회 시작일 (inclusive)
     * @param endDate   조회 종료일 (inclusive)
     * @return 날짜별 이벤트 목록
     */
    public List<CalendarDateResponse> getCalendar(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Map<LocalDate, List<CalendarEventResponse>> eventMap = new TreeMap<>();

        applicationHistoryRepository.findByUserIdAndScheduledAtBetween(userId, start, end)
                .forEach(h -> {
                    LocalDate date = h.getScheduledAt().toLocalDate();
                    eventMap.computeIfAbsent(date, k -> new ArrayList<>())
                            .add(CalendarEventResponse.builder()
                                    .type(CalendarEventType.INTERVIEW)
                                    .applicationId(h.getApplication().getId())
                                    .companyName(h.getApplication().getCompanyName())
                                    .label(h.getStage())
                                    .time(h.getScheduledAt().toLocalTime().format(TIME_FORMATTER))
                                    .build());
                });

        applicationRepository.findByUserIdAndDeadlineAtBetween(userId, start, end)
                .forEach(a -> {
                    LocalDate date = a.getDeadlineAt().toLocalDate();
                    eventMap.computeIfAbsent(date, k -> new ArrayList<>())
                            .add(CalendarEventResponse.builder()
                                    .type(CalendarEventType.DEADLINE)
                                    .applicationId(a.getId())
                                    .companyName(a.getCompanyName())
                                    .label("서류 마감")
                                    .time(a.getDeadlineAt().toLocalTime().format(TIME_FORMATTER))
                                    .build());
                });

        return eventMap.entrySet().stream()
                .map(entry -> CalendarDateResponse.builder()
                        .date(entry.getKey())
                        .events(entry.getValue())
                        .build())
                .toList();
    }
}