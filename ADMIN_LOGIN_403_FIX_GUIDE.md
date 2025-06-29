# ADMIN LOGIN 403 FORBIDDEN ERROR - FIX GUIDE

## Issue Description
The admin login endpoint `/api/admin/login` was returning a **403 Forbidden** error when attempting to log in with valid admin credentials.

## Root Cause
The SecurityConfig.java was missing a permit rule for the `/api/admin/login` endpoint. Since the endpoint wasn't explicitly allowed, it was caught by the `anyRequest().authenticated()` rule, which requires authentication. This created a chicken-and-egg problem where admin users couldn't authenticate because they needed to be authenticated to access the login endpoint.

## Fix Applied

### 1. Updated SecurityConfig.java
**File:** `src/main/java/com/licentarazu/turismapp/config/SecurityConfig.java`

**Change:** Added the admin login endpoint to the permitAll() rules:

```java
// ✅ AUTH ENDPOINTS - Must be FIRST for highest priority
.requestMatchers("/api/auth/login").permitAll()
.requestMatchers("/api/auth/register").permitAll()
.requestMatchers("/api/auth/forgot-password").permitAll()
.requestMatchers("/api/auth/reset-password").permitAll()
.requestMatchers("/api/auth/confirm").permitAll()
.requestMatchers("/api/auth/resend-confirmation").permitAll()
.requestMatchers("/api/auth/test-email-config").permitAll()
.requestMatchers("/api/auth/**").permitAll()  // Catch-all for any other auth endpoints

// ✅ ADMIN LOGIN ENDPOINT - Must be accessible without authentication
.requestMatchers("/api/admin/login").permitAll()
```

## How It Works

### Admin Login Flow
1. **Frontend** sends POST request to `/api/admin/login` with email/password
2. **SecurityConfig** allows the request to reach the controller (no authentication required)
3. **AdminController** validates credentials and checks admin role:
   - Finds user by email
   - Verifies user has ADMIN role
   - Authenticates using AuthenticationManager with plain text password
   - Generates JWT token for admin session
4. **Response** includes JWT token and admin user details

### Security Considerations
- Only the login endpoint is public; all other admin endpoints require authentication
- Admin role verification happens in the controller logic
- JWT tokens are required for subsequent admin operations
- Plain text passwords are used (NoOpPasswordEncoder) - intended for development only

## Testing the Fix

### Manual Test
Use the provided test script:
```bash
test-admin-login-403-fix.bat
```

### Expected Response (Success)
```json
{
  "message": "Admin login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "email": "admin@turismapp.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }
}
```

### cURL Test
```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@turismapp.com","password":"admin123"}'
```

## Prerequisites
1. **Backend running** on http://localhost:8080
2. **Admin user exists** in database with role 'ADMIN'
3. **Plain text password** configuration active (NoOpPasswordEncoder)

## Troubleshooting

### Still Getting 403 Forbidden?
1. **Restart the backend** to apply SecurityConfig changes
2. Check if another security filter is blocking the request
3. Verify the exact URL pattern matches `/api/admin/login`

### Getting 401 Unauthorized?
1. Check if admin user exists in database:
   ```sql
   USE turismdb;
   SELECT * FROM users WHERE role = 'ADMIN';
   ```
2. Verify email and password are correct
3. Ensure password is stored as plain text (not hashed)

### Getting 500 Internal Server Error?
1. Check backend logs for detailed error messages
2. Verify database connection is working
3. Check if all required dependencies are available

## Related Files
- `src/main/java/com/licentarazu/turismapp/config/SecurityConfig.java` - Security configuration
- `src/main/java/com/licentarazu/turismapp/controller/AdminController.java` - Admin endpoints
- `create-admin-plaintext.sql` - Script to create admin user
- `test-admin-login-403-fix.bat` - Test script for this fix

## Next Steps
After successful admin login:
1. Use the returned JWT token for subsequent admin API calls
2. Access admin dashboard at frontend
3. Test admin operations (view/approve/reject applications)

## Security Notes
- **Production Warning:** NoOpPasswordEncoder is deprecated and insecure
- **Recommendation:** Use BCryptPasswordEncoder in production
- **CORS:** Admin endpoints are protected by CORS configuration
- **JWT:** Tokens expire based on JWT configuration settings
