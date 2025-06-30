# 🔧 BOOKING CANCELLATION 403 FORBIDDEN ERROR - DEBUG GUIDE

## Issue Description
The booking cancellation endpoint `/api/bookings/{id}/cancel` is returning a **403 Forbidden** error when users try to cancel their bookings.

## Root Cause Analysis Steps

### Step 1: Check Backend Logs
1. **Start the backend** with logging enabled:
   ```bash
   cd "d:\razu\Licenta\SCD\TurismApp"
   mvn spring-boot:run
   ```

2. **Look for these log messages** when attempting to cancel a booking:
   ```
   🔵 JWT Filter - PUT /api/bookings/{id}/cancel, Auth Header: Present/Missing
   🔵 JWT Filter - Extracted email: {email}
   ✅ JWT Filter - Authentication set for: {email}
   🔵 Cancel booking request: bookingId={id}, user={email}
   ```

3. **If you see these error messages**:
   ```
   ❌ JWT Filter - No valid Bearer token for booking request
   ❌ JWT Filter - Token validation failed for: {email}
   ❌ JWT Filter - Exception during authentication: {error}
   ```

### Step 2: Test Authentication
1. **Test auth endpoint** to verify JWT token works:
   ```bash
   # Get your JWT token from browser localStorage
   curl -X GET http://localhost:8080/api/bookings/debug/auth \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json"
   ```

2. **Expected response**:
   ```json
   {
     "authenticated": true,
     "email": "user@example.com",
     "role": "GUEST",
     "id": 123,
     "enabled": true
   }
   ```

### Step 3: Test Frontend Token Sending
1. **Open browser Developer Tools** (F12)
2. **Go to Network tab**
3. **Attempt to cancel a booking**
4. **Check the request** for:
   - ✅ `Authorization: Bearer {token}` header present
   - ✅ Request URL: `PUT /api/bookings/{id}/cancel`
   - ✅ Request Origin: `http://localhost:5173`

### Step 4: Verify Token in LocalStorage
1. **Open browser Console** (F12)
2. **Run**: `localStorage.getItem('token')`
3. **Should return**: A JWT token string (starts with `eyJ`)
4. **If null**: User needs to log in again

### Step 5: Check CORS Configuration
1. **Look for CORS errors** in browser console:
   ```
   Access to XMLHttpRequest at 'http://localhost:8080/api/bookings/1/cancel' 
   from origin 'http://localhost:5173' has been blocked by CORS policy
   ```

2. **Test OPTIONS preflight**:
   ```bash
   curl -X OPTIONS http://localhost:8080/api/bookings/1/cancel \
     -H "Origin: http://localhost:5173" \
     -H "Access-Control-Request-Method: PUT" \
     -H "Access-Control-Request-Headers: Authorization,Content-Type" \
     -v
   ```

## Common Fixes

### Fix 1: JWT Token Expired/Invalid
**Symptoms**: Token validation fails in logs
**Solution**: User needs to log out and log in again

### Fix 2: User Not Authorized for Specific Booking
**Symptoms**: Authentication works but specific booking returns 403
**Root Cause**: User is not the guest, owner, or admin for that booking
**Solution**: Verify booking ownership in database

### Fix 3: CORS Preflight Issues
**Symptoms**: Browser blocks request before it reaches backend
**Solution**: Ensure SecurityConfig.java has proper CORS setup

### Fix 4: Missing Authorization Header
**Symptoms**: No JWT token in request
**Solution**: Check axios.js interceptor and localStorage

## Quick Test Commands

### Test Backend Health
```bash
curl http://localhost:8080/actuator/health
```

### Test Login and Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"your-email","password":"your-password"}'
```

### Test Auth Endpoint
```bash
curl -X GET http://localhost:8080/api/bookings/debug/auth \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test Booking Cancellation
```bash
curl -X PUT http://localhost:8080/api/bookings/1/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -v
```

## Expected Behavior

### Success Flow:
1. **JWT Filter**: Validates token ✅
2. **Authentication**: Sets user context ✅  
3. **Authorization**: Checks user can cancel this booking ✅
4. **Business Logic**: Updates booking status to CANCELLED ✅
5. **Email**: Sends notifications ✅
6. **Response**: Returns success message ✅

### Failure Points:
1. **403 at JWT Filter**: Token invalid/expired
2. **403 at Controller**: User not authorized for this specific booking
3. **400 at Business Logic**: Booking already cancelled/completed
4. **500 at Database**: System error

## Debug Script
Run the provided debug script:
```bash
debug-booking-cancellation-403.bat
```

## Next Steps
1. Follow the debug steps above
2. Check the specific error messages in logs
3. Apply the appropriate fix based on the root cause
4. Test the booking cancellation flow end-to-end

## Files Modified for Debugging
- `src/main/java/com/licentarazu/turismapp/security/JwtAuthenticationFilter.java` - Added detailed logging
- `src/main/java/com/licentarazu/turismapp/controller/BookingController.java` - Added debug endpoint
- `New front/src/services/axios.js` - Added request/response logging
- `debug-booking-cancellation-403.bat` - Debug script
