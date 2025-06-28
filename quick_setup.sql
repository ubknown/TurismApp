-- Quick Database Setup for Tourism App
-- Run this in MySQL Workbench first

CREATE DATABASE IF NOT EXISTS tourism_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE tourism_db;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    role VARCHAR(255) NOT NULL DEFAULT 'GUEST',
    INDEX idx_users_email (email)
);

-- Confirmation tokens table
CREATE TABLE confirmation_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    confirmed_at DATETIME(6),
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Password reset tokens table
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6),
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Accommodation units table
CREATE TABLE accommodation_units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT(1000),
    location VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    price_per_night DOUBLE NOT NULL,
    capacity INT NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATE,
    type VARCHAR(255),
    rating DOUBLE NOT NULL DEFAULT 0.0,
    review_count INT NOT NULL DEFAULT 0,
    total_bookings INT NOT NULL DEFAULT 0,
    monthly_revenue DOUBLE NOT NULL DEFAULT 0.0,
    status VARCHAR(255) NOT NULL DEFAULT 'active',
    owner_id BIGINT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Accommodation unit images
CREATE TABLE accommodation_units_images (
    accommodation_unit_id BIGINT NOT NULL,
    images VARCHAR(500) NOT NULL,
    FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) ON DELETE CASCADE
);

-- Accommodation unit amenities
CREATE TABLE accommodation_units_amenities (
    accommodation_unit_id BIGINT NOT NULL,
    amenities VARCHAR(255) NOT NULL,
    FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) ON DELETE CASCADE
);

-- Bookings table
CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    accommodation_unit_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_name VARCHAR(255),
    guest_email VARCHAR(255),
    total_price DOUBLE,
    FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) ON DELETE CASCADE
);

-- Reviews table
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    accommodation_unit_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT(1000),
    created_at DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (accommodation_unit_id) REFERENCES accommodation_units(id) ON DELETE CASCADE,
    UNIQUE KEY uk_reviews_user_unit (user_id, accommodation_unit_id)
);

-- Verify tables were created
SHOW TABLES;
