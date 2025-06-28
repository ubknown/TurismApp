-- Fix admin user with correct password hash for 'admin123'
USE turismdb;

-- First, let's see what we currently have
SELECT id, email, password, enabled, role FROM users WHERE email = 'admin@tourism.com';

-- Delete the existing admin user with wrong hash
DELETE FROM users WHERE email = 'admin@tourism.com';

-- Insert admin user with CORRECT hash for 'admin123'
-- Using the newly generated hash that actually works
INSERT INTO users (first_name, last_name, email, password, enabled, role, created_at)
VALUES ('Admin', 'User', 'admin@tourism.com', 
        '$2a$10$GTUA08wkyZ6qkjN/vdK8buT7ZtSV9tDUrd0RTjTUjgB9o2XmJQWs.', 
        TRUE, 'ADMIN', NOW());

-- Verify the fix
SELECT id, email, password, enabled, role, created_at FROM users WHERE email = 'admin@tourism.com';

-- Show all users for reference
SELECT id, email, enabled, role, created_at FROM users ORDER BY created_at;
