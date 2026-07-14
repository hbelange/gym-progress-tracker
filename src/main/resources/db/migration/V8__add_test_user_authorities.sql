INSERT INTO authorities
(user_id, authority)
VALUES
((SELECT id FROM users WHERE username = 'testuser'), 'write');