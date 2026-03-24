CREATE TABLE IF NOT EXISTS aicareer.user_skills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_name VARCHAR(255) NOT NULL,
    level VARCHAR(50),
    CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);