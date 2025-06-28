-- ========================================
-- Database Schema Fix for Reviews Table
-- ========================================

-- Option 1: Add missing foreign key column if it doesn't exist
-- (Run this if you want to keep existing data)

-- Check if accommodation_unit_id column exists
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'turismdb' 
  AND TABLE_NAME = 'reviews' 
  AND COLUMN_NAME = 'accommodation_unit_id';

-- If the column doesn't exist, add it
ALTER TABLE reviews 
ADD COLUMN accommodation_unit_id BIGINT;

-- Add foreign key constraint
ALTER TABLE reviews 
ADD CONSTRAINT fk_review_accommodation_unit 
FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) 
ON DELETE CASCADE;

-- Make the column NOT NULL (after you populate it with data)
-- ALTER TABLE reviews MODIFY accommodation_unit_id BIGINT NOT NULL;

-- ========================================
-- Option 2: Drop and recreate reviews table
-- (Use this if you're okay with losing review data)
-- ========================================

-- DROP TABLE IF EXISTS reviews;

-- The table will be recreated automatically by Hibernate with the correct schema
-- when you restart the application with ddl-auto=update

-- ========================================
-- Verification Queries
-- ========================================

-- Check table structure
DESCRIBE reviews;

-- Check foreign key constraints
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_SCHEMA = 'turismdb'
  AND TABLE_NAME = 'reviews';

-- Check if there are any reviews
SELECT COUNT(*) as review_count FROM reviews;

-- Check accommodation units
SELECT COUNT(*) as unit_count FROM accommodation_units;
