-- Migration: Add Owner Application System
-- Date: 2025-06-28
-- Description: Add owner_status to users table and create owner_applications table

-- Add owner_status column to users table
ALTER TABLE users ADD COLUMN owner_status VARCHAR(20) NOT NULL DEFAULT 'NONE';

-- Create owner_applications table
CREATE TABLE owner_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    review_notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_submitted_at (submitted_at)
);

-- Update existing users to have proper owner_status
-- Users with OWNER role should have APPROVED status
UPDATE users SET owner_status = 'APPROVED' WHERE role = 'OWNER';

-- Users with GUEST role should have NONE status (already default)
-- UPDATE users SET owner_status = 'NONE' WHERE role = 'GUEST'; -- Not needed due to default

-- Add constraints for owner_status values
ALTER TABLE users ADD CONSTRAINT chk_owner_status 
    CHECK (owner_status IN ('NONE', 'PENDING', 'APPROVED', 'REJECTED'));

-- Add constraints for application status values
ALTER TABLE owner_applications ADD CONSTRAINT chk_application_status 
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'));

-- Create indexes for better performance
CREATE INDEX idx_users_owner_status ON users(owner_status);
CREATE INDEX idx_users_role_owner_status ON users(role, owner_status);
