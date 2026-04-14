CREATE TABLE job_postings
(
    id                BIGSERIAL PRIMARY KEY,
    source_url        TEXT,
    company_name      VARCHAR(255),
    job_title         VARCHAR(255),
    location          VARCHAR(255),
    description       TEXT        NOT NULL,
    status            VARCHAR(50)  NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL
);

CREATE INDEX idx_job_postings_status ON job_postings (status);
CREATE INDEX idx_job_postings_created_at ON job_postings (created_at DESC);