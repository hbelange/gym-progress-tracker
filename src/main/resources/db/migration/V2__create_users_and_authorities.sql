CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  enabled INT NOT NULL);

CREATE TABLE IF NOT EXISTS authorities (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  authority VARCHAR(45) NOT NULL);

INSERT INTO users
(username, password, enabled) 
VALUES 
('harrison', '$2a$12$DMYZUF9qBMtlF1Y8scUWSuAcIn39Inp.yWQqSzHIKDSptEfrRu4bO', '1'); /* password is "password" hashed with BCrypt */

INSERT INTO authorities
(user_id, authority)
VALUES
((SELECT id FROM users WHERE username = 'harrison'), 'write');
