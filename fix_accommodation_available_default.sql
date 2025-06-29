-- ====================================================================
-- Migration Script: Fix Accommodation Units Available Field Default
-- This script ensures all accommodation units have proper default values
-- ====================================================================

USE turismdb;

-- 1. Add county and phone columns if they don't exist
ALTER TABLE accommodation_units 
ADD COLUMN IF NOT EXISTS county VARCHAR(255) NULL COMMENT 'County/Județ field' AFTER location;

ALTER TABLE accommodation_units 
ADD COLUMN IF NOT EXISTS phone VARCHAR(50) NULL COMMENT 'Contact phone number' AFTER county;

-- 2. Ensure available column has proper default value
ALTER TABLE accommodation_units 
MODIFY COLUMN available BOOLEAN NOT NULL DEFAULT TRUE;

-- 3. Update any existing NULL or FALSE available values to TRUE for active units
UPDATE accommodation_units 
SET available = TRUE 
WHERE available IS NULL OR (available = FALSE AND status = 'active');

-- 4. Ensure all units have a proper status
UPDATE accommodation_units 
SET status = 'active' 
WHERE status IS NULL OR status = '';

-- 5. Ensure all units have proper default values for metrics
UPDATE accommodation_units 
SET rating = 0.0 
WHERE rating IS NULL;

UPDATE accommodation_units 
SET review_count = 0 
WHERE review_count IS NULL;

UPDATE accommodation_units 
SET total_bookings = 0 
WHERE total_bookings IS NULL;

UPDATE accommodation_units 
SET monthly_revenue = 0.0 
WHERE monthly_revenue IS NULL;

-- 6. Ensure created_at is set for all units
UPDATE accommodation_units 
SET created_at = CURDATE() 
WHERE created_at IS NULL;

-- 7. Add indexes for new columns
CREATE INDEX IF NOT EXISTS idx_accommodation_units_county ON accommodation_units(county);
CREATE INDEX IF NOT EXISTS idx_accommodation_units_phone ON accommodation_units(phone);

-- 8. Verify the changes
SELECT 
    COUNT(*) as total_units,
    SUM(CASE WHEN available = TRUE THEN 1 ELSE 0 END) as available_units,
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as active_units,
    SUM(CASE WHEN county IS NOT NULL THEN 1 ELSE 0 END) as units_with_county,
    SUM(CASE WHEN phone IS NOT NULL THEN 1 ELSE 0 END) as units_with_phone
FROM accommodation_units;

COMMIT;

-- Success message
SELECT 'Migration completed successfully! All accommodation units now have proper defaults.' as status;
