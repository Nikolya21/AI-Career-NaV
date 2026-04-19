CREATE SCHEMA IF NOT EXISTS aicareer;

CREATE TABLE IF NOT EXISTS aicareer.questions (
    id BIGSERIAL PRIMARY KEY,
    text Text UNIQUE,
    difficulty Text
);