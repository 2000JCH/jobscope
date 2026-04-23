-- Phase 2.5: 어드민 역할 구분, 마지막 로그인 추적, 공지사항 관리

ALTER TABLE users
    ADD COLUMN role         ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    ADD COLUMN last_login_at DATETIME;

CREATE TABLE notice (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_notice_active (active)
);