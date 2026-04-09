CREATE TABLE IF NOT EXISTS aicareer.roadmap (

    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    week_number INTEGER NOT NULL,
    field_1 TEXT NOT NULL,
    field_2 TEXT NOT NULL,
    field_3 TEXT NOT NULL,
    field_4 TEXT NOT NULL,
    field_5 TEXT NOT NULL,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES aicareer.users(id) ON DELETE CASCADE,

    UNIQUE(user_id, week_number)
);