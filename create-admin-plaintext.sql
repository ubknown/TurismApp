-- Create admin user with plain text password
-- WARNING: This is NOT secure for production! Only for development/testing.

USE turismdb;

-- Delete existing admin user if exists
DELETE FROM users WHERE email = 'admin@turismapp.com';

-- Insert admin user with plain text password
INSERT INTO users (first_name, last_name, email, password, enabled, role, owner_status, created_at) 
VALUES (
    'Admin', 
    'User', 
    'admin@turismapp.com', 
    'admin123',  -- Plain text password (no encoding)
    TRUE, 
    'ADMIN', 
    'NONE',
    NOW()
);

-- Verify the admin user was created
SELECT id, first_name, last_name, email, password, enabled, role, owner_status
FROM users 
WHERE email = 'admin@tourism.com';

-- Expected result: password column should show 'admin123' in plain text
