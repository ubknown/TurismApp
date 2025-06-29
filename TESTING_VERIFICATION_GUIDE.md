# Tourism App Database & Entity Verification Guide

## ✅ Database Schema Verification

### Current Entity Status:
- ✅ **User** - Properly mapped with foreign key relationships
- ✅ **ConfirmationToken** - Correctly linked to User (user_id FK)
- ✅ **PasswordResetToken** - Correctly linked to User (user_id FK)  
- ✅ **AccommodationUnit** - Linked to User as owner (owner_id FK)
- ✅ **Booking** - Linked to AccommodationUnit (accommodation_unit_id FK)
- ✅ **Review** - Linked to both User and AccommodationUnit
- ✅ **Element Collections** - Images and amenities for AccommodationUnit

### Database Configuration:
- ✅ Database name: `turismdb` (updated in application.properties)
- ✅ DDL auto set to `none` (using manual schema)
- ✅ SQL logging enabled for debugging

## 🔧 Entity Relationship Verification

### User Entity Relationships:
```java
// User has many confirmation tokens (for email verification)
@OneToMany(mappedBy = "user") → ConfirmationToken

// User has many password reset tokens  
@OneToMany(mappedBy = "user") → PasswordResetToken

// User owns many accommodation units (if role = OWNER)
@OneToMany(mappedBy = "owner") → AccommodationUnit

// User can write many reviews
@OneToMany(mappedBy = "user") → Review
```

### AccommodationUnit Entity Relationships:
```java
// AccommodationUnit belongs to one owner
@ManyToOne → User (owner)

// AccommodationUnit has many bookings
@OneToMany(mappedBy = "accommodationUnit") → Booking

// AccommodationUnit has many reviews
@OneToMany(mappedBy = "accommodationUnit") → Review

// AccommodationUnit has collections of images and amenities
@ElementCollection → List<String> images
@ElementCollection → List<String> amenities
```

### Other Entity Relationships:
```java
// Booking belongs to one accommodation unit
Booking @ManyToOne → AccommodationUnit

// Review belongs to one user and one accommodation unit
Review @ManyToOne → User
Review @ManyToOne → AccommodationUnit

// Tokens belong to one user each
ConfirmationToken @ManyToOne → User
PasswordResetToken @ManyToOne → User
```

## 🧪 Testing Guide

### 1. Database Connection Test

**Test Application Startup:**
```bash
cd c:\Users\razvi\Desktop\SCD\TurismApp
mvn spring-boot:run
```

**Expected Results:**
- ✅ Application starts without errors
- ✅ Database connection established
- ✅ No JPA/Hibernate mapping errors
- ✅ SQL queries logged (if enabled)

### 2. Authentication Flow Tests

#### A. User Registration Test
**Postman Request:**
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe", 
    "email": "john.doe@test.com",
    "password": "password123",
    "addressCounty": "Brasov",
    "addressCity": "Brasov",
    "role": "OWNER"
}
```

**Expected Results:**
- ✅ Status: 201 Created
- ✅ User created in `users` table with `enabled=false`
- ✅ ConfirmationToken created in `confirmation_tokens` table
- ✅ Email sent (check console logs)

**Database Verification:**
```sql
-- Check user was created
SELECT * FROM users WHERE email = 'john.doe@test.com';

-- Check confirmation token was created
SELECT ct.*, u.email 
FROM confirmation_tokens ct 
JOIN users u ON ct.user_id = u.id 
WHERE u.email = 'john.doe@test.com';
```

#### B. Email Confirmation Test
**Postman Request:**
```http
GET http://localhost:8080/api/auth/confirm?token={TOKEN_FROM_DATABASE}
```

**Expected Results:**
- ✅ Status: 200 OK
- ✅ User `enabled` field updated to `true`
- ✅ Token `confirmed_at` field updated

#### C. Login Test
**Postman Request:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "john.doe@test.com",
    "password": "password123"
}
```

**Expected Results:**
- ✅ Status: 200 OK
- ✅ JWT token returned
- ✅ User details returned

#### D. Password Reset Test
**Step 1 - Request Reset:**
```http
POST http://localhost:8080/api/auth/forgot-password
Content-Type: application/json

{
    "email": "john.doe@test.com"
}
```

**Step 2 - Reset Password:**
```http
POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{
    "token": "{TOKEN_FROM_DATABASE}",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
}
```

### 3. Accommodation Unit Tests

#### A. Create Accommodation Unit
**Postman Request:**
```http
POST http://localhost:8080/api/accommodation-units
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "name": "Mountain Cabin",
    "description": "Beautiful cabin in the mountains",
    "location": "Brasov Mountains",
    "latitude": 45.6579,
    "longitude": 25.6012,
    "pricePerNight": 150.0,
    "capacity": 4,
    "type": "Cabin",
    "images": ["cabin1.jpg", "cabin2.jpg"],
    "amenities": ["WiFi", "Parking", "Kitchen"]
}
```

**Database Verification:**
```sql
-- Check accommodation unit was created
SELECT au.*, u.email as owner_email 
FROM accommodation_units au 
JOIN users u ON au.owner_id = u.id 
WHERE au.name = 'Mountain Cabin';

-- Check images were saved
SELECT * FROM accommodation_units_images 
WHERE accommodation_unit_id = {UNIT_ID};

-- Check amenities were saved  
SELECT * FROM accommodation_units_amenities 
WHERE accommodation_unit_id = {UNIT_ID};
```

### 4. Booking System Tests

#### A. Create Booking
**Postman Request:**
```http
POST http://localhost:8080/api/bookings
Content-Type: application/json

{
    "accommodationUnitId": {UNIT_ID},
    "checkInDate": "2025-07-15",
    "checkOutDate": "2025-07-20",
    "guestName": "Jane Smith",
    "guestEmail": "jane.smith@test.com",
    "totalPrice": 750.0
}
```

**Database Verification:**
```sql
-- Check booking was created
SELECT b.*, au.name as unit_name 
FROM booking b 
JOIN accommodation_units au ON b.accommodation_unit_id = au.id 
WHERE b.guest_email = 'jane.smith@test.com';

-- Check accommodation unit statistics updated
SELECT name, total_bookings, monthly_revenue 
FROM accommodation_units 
WHERE id = {UNIT_ID};
```

### 5. Review System Tests

#### A. Create Review
**Postman Request:**
```http
POST http://localhost:8080/api/reviews
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "accommodationUnitId": {UNIT_ID},
    "rating": 5,
    "comment": "Amazing place, highly recommended!"
}
```

**Database Verification:**
```sql
-- Check review was created
SELECT r.*, u.email as reviewer_email, au.name as unit_name
FROM reviews r 
JOIN users u ON r.user_id = u.id 
JOIN accommodation_units au ON r.accommodation_unit_id = au.id 
WHERE u.email = 'john.doe@test.com';
```

## 🔍 Common Issues & Solutions

### Issue 1: Foreign Key Constraint Errors
**Symptoms:** `Cannot add or update a child row: a foreign key constraint fails`

**Solutions:**
- ✅ Verify parent records exist before creating child records
- ✅ Check foreign key column names match entity annotations
- ✅ Ensure IDs are properly set

### Issue 2: Table Name Mismatches
**Symptoms:** `Table 'turismdb.accommodation_unit_images' doesn't exist`

**Solutions:**
- ✅ Check @Table annotations in entities
- ✅ Verify @ElementCollection table naming
- ✅ Run schema script again if needed

### Issue 3: Column Type Mismatches  
**Symptoms:** `Data truncation` or `Incorrect datetime value`

**Solutions:**
- ✅ Verify column types in schema match Java types
- ✅ Check LocalDateTime vs LocalDate usage
- ✅ Ensure timezone handling is consistent

### Issue 4: Email Service Not Working
**Symptoms:** Registration succeeds but no email sent

**Solutions:**
- ✅ Update email credentials in application.properties
- ✅ Enable "Less secure app access" or use App Password for Gmail
- ✅ Check email service configuration

## 📊 Database Monitoring Queries

### User Statistics
```sql
SELECT 
    role,
    COUNT(*) as user_count,
    SUM(CASE WHEN enabled THEN 1 ELSE 0 END) as enabled_count
FROM users 
GROUP BY role;
```

### Booking Statistics
```sql
SELECT 
    au.name,
    au.total_bookings,
    au.monthly_revenue,
    COUNT(b.id) as actual_bookings
FROM accommodation_units au
LEFT JOIN booking b ON au.id = b.accommodation_unit_id
GROUP BY au.id, au.name;
```

### Review Statistics
```sql
SELECT 
    au.name,
    au.rating as stored_rating,
    au.review_count as stored_count,
    COUNT(r.id) as actual_reviews,
    AVG(r.rating) as calculated_rating
FROM accommodation_units au
LEFT JOIN reviews r ON au.id = r.accommodation_unit_id
GROUP BY au.id, au.name;
```

### Token Cleanup Status
```sql
-- Expired confirmation tokens
SELECT COUNT(*) as expired_confirmation_tokens
FROM confirmation_tokens 
WHERE expires_at < NOW() AND confirmed_at IS NULL;

-- Expired password reset tokens
SELECT COUNT(*) as expired_reset_tokens
FROM password_reset_tokens 
WHERE expires_at < NOW() AND used_at IS NULL;
```

## ✅ Final Verification Checklist

- [ ] Application starts without errors
- [ ] All entity relationships work correctly
- [ ] User registration creates user and confirmation token
- [ ] Email confirmation enables user account
- [ ] Login works with confirmed accounts
- [ ] Password reset flow works end-to-end
- [ ] Accommodation units can be created by owners
- [ ] Bookings can be created and linked properly
- [ ] Reviews can be created and associated correctly
- [ ] Element collections (images/amenities) save properly
- [ ] Foreign key constraints are enforced
- [ ] Database statistics update automatically

## 🚀 Performance Optimization

After verifying functionality, consider these optimizations:

1. **Add Database Indexes:**
```sql
-- Additional performance indexes
CREATE INDEX idx_bookings_dates ON booking(check_in_date, check_out_date);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_accommodation_units_location_price ON accommodation_units(location, price_per_night);
```

2. **Enable Query Caching:**
```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.ehcache.EhCacheRegionFactory
```

3. **Connection Pool Optimization:**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

This guide should help you thoroughly verify that your MySQL database schema matches your Spring Boot entity structure and that all relationships work correctly.
