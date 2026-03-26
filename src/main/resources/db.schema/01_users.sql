CREATE SCHEMA IF NOT EXISTS aicareer;

CREATE TABLE IF NOT EXISTS aicareer.users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    vacancy_now TEXT,
    vacancy_requirements TEXT,
    test_result TEXT,
    adaptation_course TEXT,
    roadmap_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

