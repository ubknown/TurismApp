-- DATABASE FIX FOR USER DELETION
-- Run this SQL command in your MySQL client

USE turismdb;

-- Make user_id column nullable in owner_applications table
ALTER TABLE owner_applications MODIFY COLUMN user_id bigint NULL;

-- Verify the change
DESCRIBE owner_applications;

-- You should see 'YES' in the Null column for user_id after running this
