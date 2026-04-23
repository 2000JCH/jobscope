package com.jobscope.domain.notice.service;

import com.jobscope.domain.notice.dto.request.CreateNoticeRequest;
import com.jobscope.domain.notice.dto.request.UpdateNoticeRequest;
import com.jobscope.domain.notice.dto.response.NoticeResponse;
import com.jobscope.domain.notice.entity.Notice;
import com.jobscope.domain.notice.repository.NoticeRepository;
import com.jobscope.global.common.exception.BusinessException;
import com.jobscope.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 최신 활성 공지사항 1건을 반환한다. 없으면 빈 Optional을 반환한다.
     */
    public Optional<NoticeResponse> getLatestActiveNotice() {
        return noticeRepository.findFirstByActiveTrueOrderByCreatedAtDesc()
                .map(NoticeResponse::from);
    }

    /**
     * 전체 공지사항 목록을 최신순으로 반환한다 (관리자용).
     */
    public List<NoticeResponse> getAllNotices() {
        return noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    /**
     * 공지사항을 생성한다 (관리자용).
     */
    @Transactional
    public NoticeResponse createNotice(CreateNoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .active(request.isActive())
                .build();
        Notice saved = noticeRepository.save(notice);
        log.info("[NoticeService] 공지사항 생성 완료 - id: {}", saved.getId());
        return NoticeResponse.from(saved);
    }

    /**
     * 공지사항을 수정한다 (관리자용).
     */
    @Transactional
    public void updateNotice(Long id, UpdateNoticeRequest request) {
        Notice notice = findNoticeById(id);
        notice.updateContent(request.getTitle(), request.getContent(), request.getActive());
        log.info("[NoticeService] 공지사항 수정 완료 - id: {}", id);
    }

    /**
     * 공지사항을 삭제한다 (관리자용).
     */
    @Transactional
    public void deleteNotice(Long id) {
        findNoticeById(id);
        noticeRepository.deleteById(id);
        log.info("[NoticeService] 공지사항 삭제 완료 - id: {}", id);
    }

    private Notice findNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }
}