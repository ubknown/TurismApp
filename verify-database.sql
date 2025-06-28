-- Database verification script
USE turismdb;

-- Show current admin user
SELECT 
    '=== CURRENT ADMIN USER ===' as info,
    id, 
    first_name,
    last_name,
    email, 
    LEFT(password, 20) as password_start,
    enabled, 
    role, 
    created_at
FROM users 
WHERE email = 'admin@tourism.com';

-- Show all users for context
SELECT 
    '=== ALL USERS ===' as info,
    id,
    email,
    enabled,
    role,
    created_at
FROM users 
ORDER BY created_at;

-- Check for duplicate emails or spacing issues
SELECT 
    '=== EMAIL DUPLICATES CHECK ===' as info,
    email,
    COUNT(*) as count
FROM users 
GROUP BY email 
HAVING COUNT(*) > 1;

-- Check for emails with spaces or unusual characters
SELECT 
    '=== EMAIL FORMAT CHECK ===' as info,
    id,
    email,
    LENGTH(email) as email_length,
    TRIM(email) as trimmed_email,
    CASE 
        WHEN email = TRIM(email) THEN 'OK'
        ELSE 'HAS_SPACES'
    END as email_status
FROM users 
WHERE email LIKE '%admin%';

-- Quick fix if admin user doesn't exist or has wrong hash
INSERT IGNORE INTO users (first_name, last_name, email, password, enabled, role, created_at)
VALUES ('Admin', 'User', 'admin@tourism.com', 
        '$2a$10$GTUA08wkyZ6qkjN/vdK8buT7ZtSV9tDUrd0RTjTUjgB9o2XmJQWs.', 
        TRUE, 'ADMIN', NOW())
ON DUPLICATE KEY UPDATE 
    password = '$2a$10$GTUA08wkyZ6qkjN/vdK8buT7ZtSV9tDUrd0RTjTUjgB9o2XmJQWs.',
    enabled = TRUE,
    role = 'ADMIN';

-- Verify the fix worked
SELECT 
    '=== VERIFICATION AFTER FIX ===' as info,
    id, 
    email, 
    enabled, 
    role,
    CASE 
        WHEN password = '$2a$10$GTUA08wkyZ6qkjN/vdK8buT7ZtSV9tDUrd0RTjTUjgB9o2XmJQWs.' THEN 'CORRECT_HASH'
        ELSE 'WRONG_HASH'
    END as password_status
FROM users 
WHERE email = 'admin@tourism.com';
