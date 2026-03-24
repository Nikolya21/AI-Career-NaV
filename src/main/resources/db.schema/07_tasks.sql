CREATE TABLE IF NOT EXISTS aicareer.tasks (
    id BIGSERIAL PRIMARY KEY,
    week_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    urls JSONB, -- храним массив URL в формате JSON
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_week FOREIGN KEY (week_id) REFERENCES weeks(id) ON DELETE CASCADE
);