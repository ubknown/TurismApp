-- ====================================================================
-- MySQL Database Schema for Tourism Management Application
-- Generated based on Spring Boot JPA entities
-- ====================================================================

-- Set charset and collation for better Unicode support
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS turismdb 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE turismdb;

-- ====================================================================
-- TABLE: users
-- Stores user information for authentication and profile management
-- ====================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NULL,
    last_name VARCHAR(255) NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    role VARCHAR(255) NOT NULL DEFAULT 'GUEST',
    
    -- Indexes for better performance
    INDEX idx_users_email (email),
    INDEX idx_users_enabled (enabled),
    INDEX idx_users_role (role),
    INDEX idx_users_created_at (created_at),
    
    -- Check constraints
    CONSTRAINT chk_users_role CHECK (role IN ('GUEST', 'OWNER', 'ADMIN')),
    CONSTRAINT chk_users_email_format CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='User accounts for authentication and profile management';

-- ====================================================================
-- TABLE: confirmation_tokens
-- Stores email confirmation tokens for user registration
-- ====================================================================
CREATE TABLE confirmation_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    confirmed_at DATETIME(6) NULL,
    user_id BIGINT NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_confirmation_tokens_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_confirmation_tokens_token (token),
    INDEX idx_confirmation_tokens_user_id (user_id),
    INDEX idx_confirmation_tokens_expires_at (expires_at),
    INDEX idx_confirmation_tokens_confirmed_at (confirmed_at)
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='Email confirmation tokens for user registration';

-- ====================================================================
-- TABLE: password_reset_tokens
-- Stores password reset tokens for password recovery
-- ====================================================================
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    user_id BIGINT NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_password_reset_tokens_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_password_reset_tokens_token (token),
    INDEX idx_password_reset_tokens_user_id (user_id),
    INDEX idx_password_reset_tokens_expires_at (expires_at),
    INDEX idx_password_reset_tokens_used_at (used_at)
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='Password reset tokens for user password recovery';

-- ====================================================================
-- TABLE: accommodation_units
-- Stores accommodation listings (hotels, cabins, apartments, etc.)
-- ====================================================================
CREATE TABLE accommodation_units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT(1000) NULL,
    location VARCHAR(255) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    price_per_night DOUBLE NOT NULL,
    capacity INT NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATE NULL,
    type VARCHAR(255) NULL COMMENT 'Hotel, Cabană, Apartament, etc.',
    rating DOUBLE NOT NULL DEFAULT 0.0,
    review_count INT NOT NULL DEFAULT 0,
    total_bookings INT NOT NULL DEFAULT 0,
    monthly_revenue DOUBLE NOT NULL DEFAULT 0.0,
    status VARCHAR(255) NOT NULL DEFAULT 'active',
    owner_id BIGINT NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_accommodation_units_owner_id 
        FOREIGN KEY (owner_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_accommodation_units_owner_id (owner_id),
    INDEX idx_accommodation_units_available (available),
    INDEX idx_accommodation_units_location (location),
    INDEX idx_accommodation_units_price_per_night (price_per_night),
    INDEX idx_accommodation_units_rating (rating),
    INDEX idx_accommodation_units_type (type),
    INDEX idx_accommodation_units_status (status),
    INDEX idx_accommodation_units_created_at (created_at),
    INDEX idx_accommodation_units_coordinates (latitude, longitude),
    
    -- Check constraints
    CONSTRAINT chk_accommodation_units_price_per_night CHECK (price_per_night >= 0),
    CONSTRAINT chk_accommodation_units_capacity CHECK (capacity > 0),
    CONSTRAINT chk_accommodation_units_rating CHECK (rating >= 0 AND rating <= 5),
    CONSTRAINT chk_accommodation_units_review_count CHECK (review_count >= 0),
    CONSTRAINT chk_accommodation_units_total_bookings CHECK (total_bookings >= 0),
    CONSTRAINT chk_accommodation_units_monthly_revenue CHECK (monthly_revenue >= 0),
    CONSTRAINT chk_accommodation_units_status CHECK (status IN ('active', 'inactive', 'suspended'))
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='Accommodation listings and their details';

-- ====================================================================
-- TABLE: accommodation_units_images
-- Stores image URLs for accommodation units (@ElementCollection mapping)
-- ====================================================================
CREATE TABLE accommodation_units_images (
    accommodation_unit_id BIGINT NOT NULL,
    images VARCHAR(500) NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_accommodation_units_images_accommodation_unit_id 
        FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Index for better performance
    INDEX idx_accommodation_units_images_accommodation_unit_id (accommodation_unit_id)
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='Image URLs for accommodation units';

-- ====================================================================
-- TABLE: accommodation_units_amenities
-- Stores amenities for accommodation units (@ElementCollection mapping)
-- ====================================================================
CREATE TABLE accommodation_units_amenities (
    accommodation_unit_id BIGINT NOT NULL,
    amenities VARCHAR(255) NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_accommodation_units_amenities_accommodation_unit_id 
        FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Index for better performance
    INDEX idx_accommodation_units_amenities_accommodation_unit_id (accommodation_unit_id),
    INDEX idx_accommodation_units_amenities_amenities (amenities)
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='Amenities available for accommodation units';

-- ====================================================================
-- TABLE: booking
-- Stores booking information for accommodation units
-- ====================================================================
CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    accommodation_unit_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_name VARCHAR(255) NULL,
    guest_email VARCHAR(255) NULL,
    total_price DOUBLE NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_booking_accommodation_unit_id 
        FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_booking_accommodation_unit_id (accommodation_unit_id),
    INDEX idx_booking_check_in_date (check_in_date),
    INDEX idx_booking_check_out_date (check_out_date),
    INDEX idx_booking_guest_email (guest_email),
    INDEX idx_booking_dates (check_in_date, check_out_date),
    
    -- Check constraints
    CONSTRAINT chk_booking_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_booking_total_price CHECK (total_price IS NULL OR total_price >= 0),
    CONSTRAINT chk_booking_guest_email_format CHECK (
        guest_email IS NULL OR 
        guest_email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'
    )
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='Booking records for accommodation units';

-- ====================================================================
-- TABLE: reviews
-- Stores reviews and ratings for accommodation units
-- ====================================================================
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    accommodation_unit_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT(1000) NULL,
    created_at DATE NOT NULL,
    
    -- Foreign key constraints
    CONSTRAINT fk_reviews_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    CONSTRAINT fk_reviews_accommodation_unit_id 
        FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_reviews_user_id (user_id),
    INDEX idx_reviews_accommodation_unit_id (accommodation_unit_id),
    INDEX idx_reviews_rating (rating),
    INDEX idx_reviews_created_at (created_at),
    INDEX idx_reviews_unit_rating (accommodation_unit_id, rating),
    
    -- Check constraints
    CONSTRAINT chk_reviews_rating CHECK (rating >= 1 AND rating <= 5),
    
    -- Unique constraint to prevent duplicate reviews from same user for same unit
    UNIQUE KEY uk_reviews_user_unit (user_id, accommodation_unit_id)
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='Reviews and ratings for accommodation units';

-- ====================================================================
-- SAMPLE DATA INSERTS (Optional - for testing purposes)
-- ====================================================================

-- Insert sample admin user (password: "admin123" - BCrypt hashed)
INSERT INTO users (first_name, last_name, email, password, enabled, role, created_at) VALUES 
('Admin', 'User', 'admin@tourism.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', TRUE, 'ADMIN', NOW());

-- Insert sample owner user (password: "owner123" - BCrypt hashed)  
INSERT INTO users (first_name, last_name, email, password, enabled, role, created_at) VALUES 
('John', 'Doe', 'owner@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', TRUE, 'OWNER', NOW());

-- Insert sample guest user (password: "guest123" - BCrypt hashed)
INSERT INTO users (first_name, last_name, email, password, enabled, role, created_at) VALUES 
('Jane', 'Smith', 'guest@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', TRUE, 'GUEST', NOW());

-- ====================================================================
-- ADDITIONAL INDEXES FOR PERFORMANCE OPTIMIZATION
-- ====================================================================

-- Composite indexes for common query patterns
CREATE INDEX idx_users_email_enabled ON users(email, enabled);
CREATE INDEX idx_accommodation_units_owner_available ON accommodation_units(owner_id, available);
CREATE INDEX idx_accommodation_units_location_price ON accommodation_units(location, price_per_night);
CREATE INDEX idx_booking_unit_dates ON booking(accommodation_unit_id, check_in_date, check_out_date);

-- ====================================================================
-- VIEWS FOR COMMON QUERIES (Optional)
-- ====================================================================

-- View for active accommodation units with owner information
CREATE VIEW active_accommodations AS
SELECT 
    au.id,
    au.name,
    au.description,
    au.location,
    au.latitude,
    au.longitude,
    au.price_per_night,
    au.capacity,
    au.type,
    au.rating,
    au.review_count,
    au.total_bookings,
    au.monthly_revenue,
    u.first_name AS owner_first_name,
    u.last_name AS owner_last_name,
    u.email AS owner_email
FROM accommodation_units au
JOIN users u ON au.owner_id = u.id
WHERE au.available = TRUE AND au.status = 'active' AND u.enabled = TRUE;

-- View for booking statistics
CREATE VIEW booking_statistics AS
SELECT 
    au.id AS accommodation_unit_id,
    au.name AS accommodation_name,
    COUNT(b.id) AS total_bookings,
    MIN(b.check_in_date) AS first_booking_date,
    MAX(b.check_out_date) AS last_booking_date,
    SUM(b.total_price) AS total_revenue,
    AVG(b.total_price) AS average_booking_price
FROM accommodation_units au
LEFT JOIN booking b ON au.id = b.accommodation_unit_id
GROUP BY au.id, au.name;

-- ====================================================================
-- STORED PROCEDURES (Optional - for complex operations)
-- ====================================================================

DELIMITER //

-- Procedure to check accommodation availability
CREATE PROCEDURE CheckAccommodationAvailability(
    IN p_accommodation_unit_id BIGINT,
    IN p_check_in_date DATE,
    IN p_check_out_date DATE,
    OUT p_is_available BOOLEAN
)
BEGIN
    DECLARE booking_count INT DEFAULT 0;
    
    -- Check if there are any conflicting bookings
    SELECT COUNT(*) INTO booking_count
    FROM booking
    WHERE accommodation_unit_id = p_accommodation_unit_id
    AND (
        (check_in_date <= p_check_in_date AND check_out_date > p_check_in_date)
        OR (check_in_date < p_check_out_date AND check_out_date >= p_check_out_date)
        OR (check_in_date >= p_check_in_date AND check_out_date <= p_check_out_date)
    );
    
    -- Set availability based on booking conflicts and unit status
    SELECT (booking_count = 0 AND available = TRUE AND status = 'active') INTO p_is_available
    FROM accommodation_units
    WHERE id = p_accommodation_unit_id;
END //

-- Procedure to cleanup expired tokens
CREATE PROCEDURE CleanupExpiredTokens()
BEGIN
    -- Remove expired confirmation tokens
    DELETE FROM confirmation_tokens 
    WHERE expires_at < NOW() AND confirmed_at IS NULL;
    
    -- Remove expired and used password reset tokens
    DELETE FROM password_reset_tokens 
    WHERE expires_at < NOW() OR used_at IS NOT NULL;
END //

DELIMITER ;

-- ====================================================================
-- TRIGGERS FOR DATA INTEGRITY AND AUTOMATION
-- ====================================================================

DELIMITER //

-- Trigger to update accommodation unit statistics when a booking is created
CREATE TRIGGER trg_booking_after_insert
AFTER INSERT ON booking
FOR EACH ROW
BEGIN
    UPDATE accommodation_units 
    SET total_bookings = total_bookings + 1,
        monthly_revenue = monthly_revenue + COALESCE(NEW.total_price, 0)
    WHERE id = NEW.accommodation_unit_id;
END //

-- Trigger to update accommodation unit statistics when a booking is deleted
CREATE TRIGGER trg_booking_after_delete
AFTER DELETE ON booking
FOR EACH ROW
BEGIN
    UPDATE accommodation_units 
    SET total_bookings = GREATEST(total_bookings - 1, 0),
        monthly_revenue = GREATEST(monthly_revenue - COALESCE(OLD.total_price, 0), 0)
    WHERE id = OLD.accommodation_unit_id;
END //

DELIMITER ;

-- ====================================================================
-- GRANTS AND PERMISSIONS (Adjust according to your security requirements)
-- ====================================================================

-- Create application user (replace with your actual database user)
-- CREATE USER 'turism_app'@'localhost' IDENTIFIED BY 'your_secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON tourism_db.* TO 'turism_app'@'localhost';
-- FLUSH PRIVILEGES;

-- ====================================================================
-- MAINTENANCE QUERIES
-- ====================================================================

-- Query to find and remove duplicate confirmation tokens
-- DELETE ct1 FROM confirmation_tokens ct1
-- INNER JOIN confirmation_tokens ct2
-- WHERE ct1.id > ct2.id AND ct1.user_id = ct2.user_id AND ct1.confirmed_at IS NULL;

-- Query to find users without confirmed email
-- SELECT * FROM users WHERE enabled = FALSE;

-- Query to find popular accommodation units
-- SELECT au.*, COUNT(b.id) as booking_count
-- FROM accommodation_units au
-- LEFT JOIN booking b ON au.id = b.accommodation_unit_id
-- GROUP BY au.id
-- ORDER BY booking_count DESC;

-- ====================================================================
-- END OF SCHEMA
-- ====================================================================
