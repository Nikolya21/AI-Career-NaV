CREATE SCHEMA IF NOT EXISTS aicareer;

CREATE TABLE IF NOT EXISTS aicareer.tags_questions (
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    PRIMARY KEY (question_id, tag_id),

    CONSTRAINT fk_question
    FOREIGN KEY (question_id)
    REFERENCES aicareer.questions (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_tag
    FOREIGN KEY (tag_id)
    REFERENCES aicareer.tags (id)
    ON DELETE CASCADE
);