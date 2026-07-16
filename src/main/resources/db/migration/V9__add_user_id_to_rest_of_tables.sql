ALTER TABLE workout_type ADD COLUMN user_id BIGINT NOT NULL REFERENCES users(id);
ALTER TABLE workout_type ADD CONSTRAINT unique_workout_type_name_per_user UNIQUE (name, user_id);
ALTER TABLE workout_type DROP CONSTRAINT IF EXISTS workout_type_name_key;


ALTER TABLE workout ADD COLUMN user_id BIGINT NOT NULL REFERENCES users(id);

ALTER TABLE workout_set ADD COLUMN user_id BIGINT NOT NULL REFERENCES users(id);

ALTER TABLE measurement ADD COLUMN user_id BIGINT NOT NULL REFERENCES users(id); 
