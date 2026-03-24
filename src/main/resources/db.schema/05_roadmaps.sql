CREATE TABLE IF NOT EXISTS aicareer.roadmaps (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roadmaps_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Добавляем foreign key после создания таблицы roadmaps
ALTER TABLE users
    ADD CONSTRAINT fk_users_roadmap
        FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id) ON DELETE SET NULL;