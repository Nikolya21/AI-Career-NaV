CREATE TABLE IF NOT EXISTS aicareer.roadmap_weeks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    week_number INTEGER NOT NULL,
    week_topic TEXT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES aicareer.users(id) ON DELETE CASCADE
);