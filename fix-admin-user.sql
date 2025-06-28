-- ====================================================================
-- FIX ADMIN USER SCRIPT
-- This script will ensure the admin user exists with correct credentials
-- ====================================================================

USE turismdb;

-- First, let's check if the admin user exists
SELECT 'Checking existing admin user...' AS status;
SELECT id, first_name, last_name, email, enabled, role, created_at 
FROM users 
WHERE email = 'admin@tourism.com';

-- Delete any existing admin user to avoid conflicts
DELETE FROM users WHERE email = 'admin@tourism.com';

-- Create the admin user with correct BCrypt hash for password "admin123"
INSERT INTO users (first_name, last_name, email, password, enabled, role, created_at) 
VALUES (
    'Admin', 
    'User', 
    'admin@tourism.com', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
    TRUE, 
    'ADMIN', 
    NOW()
);

-- Verify the admin user was created correctly
SELECT 'Admin user created successfully:' AS status;
SELECT id, first_name, last_name, email, enabled, role, created_at 
FROM users 
WHERE email = 'admin@tourism.com';

-- Also verify other test users exist
SELECT 'All test users:' AS status;
SELECT id, first_name, last_name, email, enabled, role, created_at 
FROM users 
WHERE email IN ('admin@tourism.com', 'owner@example.com', 'guest@example.com')
ORDER BY role, email;

-- Check user count
SELECT 'Total users in database:' AS status, COUNT(*) AS user_count FROM users;
