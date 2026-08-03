CREATE TABLE pending_email_changes (
    id BIGSERIAL PRIMARY KEY,
    new_email VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE
);