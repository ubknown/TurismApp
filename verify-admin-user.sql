-- Verify admin user in database
USE turismdb;

-- Check if admin user exists
SELECT 
    id,
    first_name,
    last_name,
    email,
    password,
    enabled,
    role,
    created_at
FROM users 
WHERE email = 'admin@tourism.com';

-- Check all users to see what exists
SELECT 
    id,
    first_name,
    last_name,
    email,
    enabled,
    role,
    created_at
FROM users 
ORDER BY created_at;

-- If admin user doesn't exist, create it
INSERT IGNORE INTO users (first_name, last_name, email, password, enabled, role, created_at)
VALUES ('Admin', 'User', 'admin@tourism.com', 
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
        TRUE, 'ADMIN', NOW());

-- Verify the insert worked
SELECT 
    id,
    first_name,
    last_name,
    email,
    password,
    enabled,
    role,
    created_at
FROM users 
WHERE email = 'admin@tourism.com';
