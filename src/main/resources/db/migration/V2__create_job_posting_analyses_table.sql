CREATE TABLE job_analyses
(
    id                     BIGSERIAL PRIMARY KEY,
    job_posting_id         BIGINT       NOT NULL,
    created_at             TIMESTAMP    NOT NULL,

    role_type              VARCHAR(255),
    java_relevance         VARCHAR(255),
    seniority_level        VARCHAR(255),
    domain                 VARCHAR(255),
    vacancy_language       VARCHAR(255),
    required_german_level  VARCHAR(255),

    primary_stack          VARCHAR(1000),
    secondary_stack        VARCHAR(1000),
    nice_to_have_stack     VARCHAR(1000),
    gaps                   VARCHAR(3000),

    CONSTRAINT fk_job_posting_analyses_job_posting
        FOREIGN KEY (job_posting_id)
            REFERENCES job_postings (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_job_posting_analyses_job_posting_id
    ON job_analyses (job_posting_id);

CREATE INDEX idx_job_posting_analyses_created_at
    ON job_analyses (created_at DESC);