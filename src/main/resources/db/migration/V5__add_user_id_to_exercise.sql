ALTER TABLE exercise ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);
UPDATE exercise SET user_id = (SELECT id FROM users WHERE username = 'harrison') WHERE user_id IS NULL;
ALTER TABLE exercise ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE exercise ADD CONSTRAINT unique_exercise_name_per_user UNIQUE (name, user_id);
ALTER TABLE exercise DROP CONSTRAINT IF EXISTS exercise_name_key;