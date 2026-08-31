-- ============================================================
-- V1: 초기 스키마
-- 설계 근거 상세는 프로젝트 루트 SCHEMA_DESIGN.md, CONSIDERATIONS.md 참고
-- ============================================================

-- 1. member : 더미 로그인용 사용자
CREATE TABLE member (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL,
    password        VARCHAR(100) NOT NULL,
    display_name    VARCHAR(50) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_member_username UNIQUE (username)
);

INSERT INTO member (username, password, display_name) VALUES
    ('test1', '1234', '테스트유저1'),
    ('test2', '5678', '테스트유저2');


-- 2. extension_policy : 확장자 차단 정책 (고정 + 커스텀 통합)
CREATE TABLE extension_policy (
    id              BIGSERIAL PRIMARY KEY,
    extension       VARCHAR(20) NOT NULL,
    type            VARCHAR(10) NOT NULL,
    is_blocked      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_extension_policy_type CHECK (type IN ('FIXED', 'CUSTOM')),
    CONSTRAINT ck_extension_format CHECK (extension ~ '^[a-z0-9]+$'),
    CONSTRAINT uq_extension_policy_extension UNIQUE (extension)
);

CREATE INDEX idx_extension_policy_type_blocked
    ON extension_policy (type, is_blocked);

INSERT INTO extension_policy (extension, type, is_blocked) VALUES
    ('bat', 'FIXED', false),
    ('cmd', 'FIXED', false),
    ('com', 'FIXED', false),
    ('cpl', 'FIXED', false),
    ('exe', 'FIXED', false),
    ('scr', 'FIXED', false),
    ('js',  'FIXED', false);


-- 3. extension_policy_audit_log : 정책 변경 이력 (불변 로그)
CREATE TABLE extension_policy_audit_log (
    id                    BIGSERIAL PRIMARY KEY,
    extension             VARCHAR(20) NOT NULL,
    type                  VARCHAR(10) NOT NULL,
    action                VARCHAR(10) NOT NULL,

    changed_by_member_id  BIGINT NULL
        REFERENCES member(id) ON DELETE SET NULL,
    changed_by_username   VARCHAR(50) NOT NULL,

    changed_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_audit_log_type CHECK (type IN ('FIXED', 'CUSTOM')),
    CONSTRAINT ck_audit_log_action CHECK (action IN ('BLOCK', 'UNBLOCK', 'ADD', 'REMOVE'))
);

CREATE INDEX idx_audit_log_changed_at ON extension_policy_audit_log (changed_at DESC);
CREATE INDEX idx_audit_log_extension ON extension_policy_audit_log (extension);
CREATE INDEX idx_audit_log_changed_by_member_id ON extension_policy_audit_log (changed_by_member_id);


-- 4. uploaded_file : 실제 업로드 처리 결과
CREATE TABLE uploaded_file (
    id                      BIGSERIAL PRIMARY KEY,
    original_filename       VARCHAR(255) NOT NULL,
    stored_filename         VARCHAR(255),
    extension                VARCHAR(20),
    detected_mime            VARCHAR(100),
    magic_byte_matched       BOOLEAN,
    size_bytes                BIGINT NOT NULL,
    status                    VARCHAR(10) NOT NULL,
    reject_reason             VARCHAR(255),

    uploaded_by_member_id    BIGINT NULL
        REFERENCES member(id) ON DELETE SET NULL,
    uploaded_by_username      VARCHAR(50),

    created_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at                  TIMESTAMPTZ NULL,

    CONSTRAINT ck_uploaded_file_status CHECK (status IN ('SUCCESS', 'REJECTED')),
    CONSTRAINT ck_uploaded_file_size CHECK (size_bytes >= 0 AND size_bytes <= 10485760),
    CONSTRAINT uq_uploaded_file_stored_filename UNIQUE (stored_filename)
);

CREATE INDEX idx_uploaded_file_created_at ON uploaded_file (created_at DESC);
CREATE INDEX idx_uploaded_file_active ON uploaded_file (created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_uploaded_file_rejected ON uploaded_file (created_at DESC) WHERE status = 'REJECTED' AND deleted_at IS NULL;
CREATE INDEX idx_uploaded_file_uploaded_by_member_id ON uploaded_file (uploaded_by_member_id);
