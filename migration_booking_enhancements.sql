-- ====================================================================
-- Migration Script: Add new fields to booking table
-- Purpose: Support additional booking information and status tracking
-- ====================================================================

USE turismdb;

-- Add new columns to booking table
ALTER TABLE booking 
ADD COLUMN guest_phone VARCHAR(20) NULL COMMENT 'Guest phone number',
ADD COLUMN number_of_guests INT DEFAULT 1 COMMENT 'Number of guests for the booking',
ADD COLUMN special_requests TEXT NULL COMMENT 'Special requests from guest',
ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Booking status';

-- Add check constraint for status
ALTER TABLE booking 
ADD CONSTRAINT chk_booking_status 
CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'));

-- Add check constraint for number of guests
ALTER TABLE booking 
ADD CONSTRAINT chk_booking_number_of_guests 
CHECK (number_of_guests >= 1 AND number_of_guests <= 20);

-- Add indexes for new fields
CREATE INDEX idx_booking_status ON booking(status);
CREATE INDEX idx_booking_guest_phone ON booking(guest_phone);

-- Update existing records to have CONFIRMED status instead of NULL
UPDATE booking SET status = 'CONFIRMED' WHERE status IS NULL;

COMMIT;

-- ====================================================================
-- Verification queries (run these to verify the migration)
-- ====================================================================
-- DESCRIBE booking;
-- SELECT COUNT(*) FROM booking WHERE status = 'CONFIRMED';
-- SELECT DISTINCT status FROM booking;
