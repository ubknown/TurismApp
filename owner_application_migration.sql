-- Add owner_status column to users table
ALTER TABLE users ADD COLUMN owner_status VARCHAR(255) NOT NULL DEFAULT 'NONE';

-- Create owner_applications table
CREATE TABLE owner_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    submitted_at DATETIME(6) NOT NULL,
    reviewed_at DATETIME(6),
    review_notes TEXT,
    CONSTRAINT fk_owner_application_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX idx_owner_applications_status ON owner_applications(status);
CREATE INDEX idx_owner_applications_submitted_at ON owner_applications(submitted_at);

-- Update existing users to have default owner_status
UPDATE users SET owner_status = 'NONE' WHERE owner_status IS NULL;
