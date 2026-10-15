-- Black Hawk Security Solution — initial schema.
-- Written to run on PostgreSQL 17 and on H2 in PostgreSQL mode (tests).

CREATE TABLE admin_users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(190) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    display_name    VARCHAR(120) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL
);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    token_hash      VARCHAR(128) NOT NULL UNIQUE,
    admin_user_id   BIGINT       NOT NULL REFERENCES admin_users(id) ON DELETE CASCADE,
    expires_at      TIMESTAMP    NOT NULL,
    revoked_at      TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(admin_user_id);

CREATE TABLE reference_counters (
    ref_year        INT    NOT NULL PRIMARY KEY,
    last_value      BIGINT NOT NULL
);

CREATE TABLE enquiries (
    id               BIGSERIAL PRIMARY KEY,
    reference_code   VARCHAR(20)  NOT NULL UNIQUE,
    full_name        VARCHAR(120) NOT NULL,
    phone            VARCHAR(10)  NOT NULL,
    email            VARCHAR(190),
    organisation     VARCHAR(160),
    site_name        VARCHAR(160),
    service_category VARCHAR(40)  NOT NULL,
    location         VARCHAR(200) NOT NULL,
    preferred_date   DATE,
    personnel_count  INT,
    duration_shift   VARCHAR(200),
    requirements     TEXT,
    event_venue      VARCHAR(200),
    expected_crowd   INT,
    event_start      VARCHAR(40),
    event_end        VARCHAR(40),
    source           VARCHAR(20)  NOT NULL DEFAULT 'WEB',
    status           VARCHAR(30)  NOT NULL DEFAULT 'NEW',
    follow_up_at     TIMESTAMP,
    notify_failed    BOOLEAN      NOT NULL DEFAULT FALSE,
    reminder_count   INT          NOT NULL DEFAULT 0,
    ip_hash          VARCHAR(64),
    user_agent       VARCHAR(300),
    consent_at       TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);
CREATE INDEX idx_enquiries_status ON enquiries(status);
CREATE INDEX idx_enquiries_created ON enquiries(created_at);
CREATE INDEX idx_enquiries_phone_created ON enquiries(phone, created_at);
CREATE INDEX idx_enquiries_follow_up ON enquiries(follow_up_at);

CREATE TABLE enquiry_notes (
    id            BIGSERIAL PRIMARY KEY,
    enquiry_id    BIGINT       NOT NULL REFERENCES enquiries(id) ON DELETE CASCADE,
    author_email  VARCHAR(190) NOT NULL,
    body          TEXT         NOT NULL,
    created_at    TIMESTAMP    NOT NULL
);
CREATE INDEX idx_enquiry_notes_enquiry ON enquiry_notes(enquiry_id);

CREATE TABLE contact_messages (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(120) NOT NULL,
    phone       VARCHAR(10)  NOT NULL,
    email       VARCHAR(190),
    subject     VARCHAR(160),
    message     TEXT         NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    ip_hash     VARCHAR(64),
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE job_applications (
    id               BIGSERIAL PRIMARY KEY,
    full_name        VARCHAR(120) NOT NULL,
    phone            VARCHAR(10)  NOT NULL,
    city             VARCHAR(120) NOT NULL,
    role_applied     VARCHAR(60)  NOT NULL,
    experience_years INT,
    is_ex_serviceman BOOLEAN      NOT NULL DEFAULT FALSE,
    service_unit     VARCHAR(160),
    age              INT,
    message          TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    ip_hash          VARCHAR(64),
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);
CREATE INDEX idx_job_applications_status ON job_applications(status);

CREATE TABLE gallery_items (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(160) NOT NULL,
    category        VARCHAR(20)  NOT NULL,
    file_name       VARCHAR(200) NOT NULL,
    thumb_file_name VARCHAR(200) NOT NULL,
    width           INT          NOT NULL,
    height          INT          NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    is_published    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL
);

CREATE TABLE client_orgs (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(160) NOT NULL,
    area          VARCHAR(160),
    city          VARCHAR(80)  NOT NULL,
    sector        VARCHAR(40),
    highlight     VARCHAR(300),
    is_published  BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order    INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL
);

CREATE TABLE announcements (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(160) NOT NULL,
    body          TEXT         NOT NULL,
    starts_at     TIMESTAMP,
    ends_at       TIMESTAMP,
    is_published  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL
);
