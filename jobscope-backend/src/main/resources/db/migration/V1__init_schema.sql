-- 초기 스키마 (카카오 소셜 로그인, 지원 현황 CRUD, 전형 단계 관리, 카카오 알림, 어드민, 공지사항)

CREATE TABLE users (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    kakao_id                    VARCHAR(100)    NOT NULL,
    nickname                    VARCHAR(50)     NOT NULL,
    email                       VARCHAR(100),
    profile_image               VARCHAR(255),
    custom_profile_image        VARCHAR(500),
    refresh_token               VARCHAR(500),
    refresh_token_expires_at    DATETIME,
    role                        ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    last_login_at               DATETIME,
    created_at                  DATETIME        NOT NULL,
    updated_at                  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_kakao_id (kakao_id)
);

CREATE TABLE oauth_token (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                  BIGINT       NOT NULL,
    provider                 VARCHAR(20)  NOT NULL,
    access_token             VARCHAR(500),
    access_token_expires_at  DATETIME,
    refresh_token            VARCHAR(500),
    refresh_token_expires_at DATETIME,
    created_at               DATETIME     NOT NULL,
    updated_at               DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_oauth_token (user_id, provider),
    CONSTRAINT fk_oauth_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE application (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    company_name    VARCHAR(100)    NOT NULL,
    job_position    VARCHAR(100)    NOT NULL,
    applied_at      DATE            NOT NULL,
    deadline_at     DATETIME,
    result          ENUM('IN_PROGRESS','PASSED','FAILED') NOT NULL DEFAULT 'IN_PROGRESS',
    alarm_enabled   BOOLEAN         NOT NULL DEFAULT TRUE,
    job_posting_url VARCHAR(500),
    memo            TEXT,
    retrospective   TEXT,
    deleted_at      DATETIME,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_application_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    INDEX idx_application_user_id     (user_id),
    INDEX idx_application_deadline_at (deadline_at),
    INDEX idx_application_result      (result),
    INDEX idx_application_alarm       (alarm_enabled)
);

CREATE TABLE application_history (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    application_id  BIGINT          NOT NULL,
    stage           VARCHAR(100)    NOT NULL,
    stage_result    ENUM('PENDING','PASS','FAIL') NOT NULL DEFAULT 'PENDING',
    scheduled_at    DATETIME,
    completed_at    DATETIME,
    memo            TEXT,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_history_application
        FOREIGN KEY (application_id) REFERENCES application(id)
        ON DELETE CASCADE,
    INDEX idx_history_application_id (application_id),
    INDEX idx_history_scheduled_at   (scheduled_at)
);

CREATE TABLE alarm_log (
    id                      BIGINT      NOT NULL AUTO_INCREMENT,
    user_id                 BIGINT      NOT NULL,
    application_id          BIGINT      NOT NULL,
    application_history_id  BIGINT,
    alarm_type              ENUM('D7','D3','D1','DEADLINE') NOT NULL,
    sent_at                 DATETIME    NOT NULL,
    is_success              BOOLEAN     NOT NULL,
    created_at              DATETIME    NOT NULL,
    updated_at              DATETIME    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alarm_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_alarm_application
        FOREIGN KEY (application_id) REFERENCES application(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_alarm_history
        FOREIGN KEY (application_history_id) REFERENCES application_history(id)
        ON DELETE SET NULL,
    INDEX idx_alarm_user_id    (user_id),
    INDEX idx_alarm_sent_at    (sent_at),
    INDEX idx_alarm_history_id (application_history_id),
    UNIQUE KEY uq_alarm_prevent (user_id, application_id, alarm_type, (DATE(sent_at)))
);

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