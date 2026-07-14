CREATE TABLE workout_type (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE workout (
    id              BIGSERIAL PRIMARY KEY,
    date            DATE NOT NULL,
    workout_type_id BIGINT NOT NULL REFERENCES workout_type(id)
);

CREATE TABLE exercise (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE workout_set (
    id              BIGSERIAL PRIMARY KEY,
    workout_id      BIGINT NOT NULL REFERENCES workout(id),
    exercise_id     BIGINT NOT NULL REFERENCES exercise(id),
    reps            INT NOT NULL,
    reps_in_reserve INT,
    weight          NUMERIC(6, 2)
);

CREATE TYPE measurement_type AS ENUM ('WEIGHT', 'CALORIES', 'STEPS');

CREATE TABLE measurement (
    id    BIGSERIAL PRIMARY KEY,
    date  DATE NOT NULL,
    type  measurement_type NOT NULL,
    value NUMERIC(10, 2) NOT NULL
);