-- Run once in phpMyAdmin for databases already imported.
-- This changes only the display name of the existing admin account.
-- It preserves the password, username, sales and all other data.
USE stark_industry_db;
UPDATE users SET full_name = 'Chalaka Chamikara' WHERE username = 'admin';
SELECT user_id, username, full_name FROM users WHERE username = 'admin';
