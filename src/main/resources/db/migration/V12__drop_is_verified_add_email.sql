ALTER TABLE users DROP COLUMN is_verified;
ALTER TABLE users ADD email VARCHAR(255) UNIQUE;
UPDATE users SET email = username || '@example.com'; -- Set a default email based on the username
ALTER TABLE users ALTER COLUMN email SET NOT NULL;