-- Migration to add email field to owner_applications table and make user_id nullable
-- This preserves owner application history even after account deletion

-- Add email column
ALTER TABLE owner_applications 
ADD COLUMN email VARCHAR(255);

-- Update existing records to populate email from user table
UPDATE owner_applications oa 
SET email = (SELECT u.email FROM users u WHERE u.id = oa.user_id)
WHERE oa.email IS NULL;

-- Make email column non-null and unique after populating
ALTER TABLE owner_applications 
MODIFY COLUMN email VARCHAR(255) NOT NULL UNIQUE;

-- Make user_id nullable (allows preserving applications after user deletion)
ALTER TABLE owner_applications 
MODIFY COLUMN user_id BIGINT NULL;

-- Remove unique constraint from user_id since it can be NULL
-- Note: In newer MySQL versions, you might need to find and drop the constraint by name
-- You can find the constraint name with: SHOW CREATE TABLE owner_applications;
ALTER TABLE owner_applications 
DROP INDEX IF EXISTS user_id;
