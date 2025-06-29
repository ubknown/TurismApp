# Plain Text Password Configuration Guide

## Overview
This guide shows how to configure the Tourism App to use plain text passwords (no encoding/hashing) for admin authentication. This configuration is **NOT secure** and should only be used for development/testing purposes.

## ⚠️ Security Warning
**NEVER use plain text passwords in production!** This configuration disables password hashing entirely and stores passwords as plain text in the database.

## Configuration Changes Made

### 1. SecurityConfig.java Password Encoder
**File**: `src/main/java/com/licentarazu/turismapp/config/SecurityConfig.java`

**Before** (BCrypt encoding):
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**After** (Plain text - no encoding):
```java
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Bean
public PasswordEncoder passwordEncoder() {
    // Use NoOpPasswordEncoder for plain text passwords
    // WARNING: This is NOT secure for production! Only for development/testing.
    return NoOpPasswordEncoder.getInstance();
}
```

### 2. Manual Admin User Creation
**File**: `create-admin-plaintext.sql`

```sql
-- Insert admin user with plain text password
INSERT INTO users (first_name, last_name, email, password, enabled, role, owner_status, created_at) 
VALUES (
    'Admin', 
    'User', 
    'admin@tourism.com', 
    'admin123',  -- Plain text password (no encoding)
    TRUE, 
    'ADMIN', 
    'NONE',
    NOW()
);
```

## Setup Steps

### Step 1: Apply Configuration Changes
The SecurityConfig.java has been updated to use `NoOpPasswordEncoder.getInstance()` instead of `BCryptPasswordEncoder`.

### Step 2: Create Admin User Manually
```bash
# Run the SQL script to create admin user
mysql -u root -p turismdb < create-admin-plaintext.sql
```

### Step 3: Verify Database
```sql
SELECT email, password, role FROM users WHERE email = 'admin@tourism.com';
-- Should show: admin123 (plain text)
```

### Step 4: Test Authentication
```bash
# Run the test script
test-admin-plaintext.bat
```

## How It Works

### Authentication Flow
1. **User Login**: Admin enters `admin@tourism.com` / `admin123`
2. **Database Lookup**: System finds user with email `admin@tourism.com`
3. **Password Comparison**: `NoOpPasswordEncoder` compares:
   - Input: `admin123`
   - Database: `admin123`
   - Result: Direct string comparison (no hashing)
4. **Success**: Authentication succeeds, JWT token generated

### NoOpPasswordEncoder Behavior
```java
// NoOpPasswordEncoder.getInstance() does this:
public String encode(CharSequence rawPassword) {
    return rawPassword.toString(); // No encoding - returns as-is
}

public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return rawPassword.toString().equals(encodedPassword); // Direct comparison
}
```

## Testing Admin Dashboard

### Admin Login Endpoints
1. **AdminController** (`/api/admin/login`): Dedicated admin dashboard login
2. **AuthController** (`/api/auth/login`): General authentication (also works for admin)

### Test Commands
```bash
# Test AdminController login
curl -X POST "http://localhost:8080/api/admin/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@tourism.com","password":"admin123"}'

# Test AuthController login  
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@tourism.com","password":"admin123"}'
```

### Expected Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "admin@tourism.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }
}
```

## Admin Dashboard Access

### Frontend Access
1. Navigate to: `http://localhost:5173/admin`
2. Login with: `admin@tourism.com` / `admin123`
3. Access admin features:
   - View all owner applications
   - Approve/Reject applications
   - Manage system data

### Route Protection
The admin dashboard (`/admin`) uses a separate login form that:
- Clears any existing tokens on load
- Requires fresh admin authentication
- Uses `/api/admin/login` endpoint
- Stores admin token separately

## Verification Checklist

### ✅ Configuration Applied
- [ ] `SecurityConfig.java` uses `NoOpPasswordEncoder`
- [ ] No BCrypt imports remain
- [ ] Deprecation warnings are expected (safe to ignore)

### ✅ Database Setup
- [ ] Admin user exists in `users` table
- [ ] Password field contains `admin123` (plain text)
- [ ] Role is set to `ADMIN`
- [ ] `enabled` is `TRUE`

### ✅ Authentication Works
- [ ] Admin login via `/api/admin/login` succeeds
- [ ] Admin login via `/api/auth/login` succeeds
- [ ] Both return valid JWT tokens
- [ ] Admin dashboard accessible

### ✅ Admin Dashboard Functions
- [ ] Admin dashboard loads at `/admin`
- [ ] Requires admin login (even if logged in as other user)
- [ ] Shows all owner applications
- [ ] Accept/Refuse buttons work
- [ ] Database updates after approval/rejection
- [ ] UI refreshes after admin actions

## Important Notes

### 1. Security Impact
- **All passwords** in the system are now plain text
- This affects regular users too (guests, owners)
- Database exposure = direct password access

### 2. Development Only
- This configuration is for testing admin dashboard functionality
- **Must be reverted** before production deployment
- Consider using environment-specific configurations

### 3. Password Encoder Affects
- User registration (`UserService.registerUser`)
- Password updates (`UserService.updateUserPassword`)
- Data seeding (`DataSeeder`)
- Password reset functionality

### 4. Reverting to Secure Passwords
To revert back to secure BCrypt encoding:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

And update the admin user password to a BCrypt hash:
```sql
UPDATE users SET password = '$2a$10$...' WHERE email = 'admin@tourism.com';
```
