# ACCOUNT DELETION AND PASSWORD RESET FIX GUIDE

## ISSUES IDENTIFIED AND FIXED

### 1. **Password Reset Not Working**
- **Problem**: Password reset and forgot password functionality was broken
- **Root Cause**: Frontend components were using direct `axios` instead of the configured `api` instance
- **Fix**: 
  - Updated `ResetPasswordForm.jsx` to use `api` from `../services/axios`
  - Updated `ForgotPasswordForm.jsx` to use `api` from `../services/axios`
  - This ensures proper base URL and header configuration

### 2. **Account Deletion Database Constraints**
- **Problem**: Account deletion was failing due to database foreign key constraints
- **Root Cause**: Bookings made by the user as a guest were not being deleted before user deletion
- **Fix**:
  - Added `findByGuestEmail()` method to `BookingRepository`
  - Updated `UserService.deleteUserAccount()` to delete guest bookings first
  - Improved deletion order: Guest bookings → Owned units → Applications → Tokens → User
  - Added comprehensive logging for each deletion step

### 3. **Missing Admin Email Notification for Owner Applications**
- **Problem**: Admin was not receiving email notifications when users applied to become owners
- **Root Cause**: The `OwnerApplicationService.submitApplication()` method was NOT calling the admin notification method
- **Fix**: 
  - Added admin email configuration to `application.properties`
  - Created `sendOwnerApplicationNotificationToAdmin()` method in `EmailService`
  - Updated `submitApplication()` to call the admin notification method
  - Added comprehensive logging for troubleshooting

### 4. **Enhanced Error Handling**
- **Problem**: Generic error messages made debugging difficult
- **Root Cause**: Insufficient error handling in both frontend and backend
- **Fix**:
  - Enhanced frontend error handling with specific error messages
  - Added comprehensive backend logging with step-by-step process tracking
  - Added test endpoints for debugging

## FILES MODIFIED

### Backend Changes:

1. **`application.properties`**
   - Added: `app.admin.email=turismapplic@gmail.com`

2. **`EmailService.java`**
   - Added: `@Value("${app.admin.email:turismapplic@gmail.com}")` admin email configuration
   - Added: `sendOwnerApplicationNotificationToAdmin()` method
   - Added: `getAdminEmail()` helper method

3. **`OwnerApplicationService.java`**
   - Modified: `submitApplication()` method to send admin notification emails
   - Added: Comprehensive logging and error handling

4. **`UserService.java`**
   - Added: BookingRepository dependency for guest booking deletion
   - Enhanced: `deleteUserAccount()` method with proper deletion order
   - Added: Comprehensive step-by-step logging

5. **`BookingRepository.java`**
   - Added: `findByGuestEmail(String guestEmail)` method

6. **`AuthController.java`**
   - Enhanced: `deleteAccount()` method with better error handling
   - Added: `testAdminNotificationEmail()` endpoint for testing
   - Added: `testDeleteAccount()` endpoint for debugging

### Frontend Changes:

7. **`ResetPasswordForm.jsx`**
   - Fixed: Import and use `api` from `../services/axios` instead of direct axios
   - This ensures proper base URL and authentication headers

8. **`ForgotPasswordForm.jsx`**
   - Fixed: Import and use `api` from `../services/axios` instead of direct axios
   - This ensures proper base URL and authentication headers

9. **`NavBar.jsx`**
   - Enhanced: `handleDeleteAccount()` with specific error handling
   - Added: Console logging for debugging
   - Added: HTTP status code specific error messages

### Test Files:

10. **`test-delete-account.bat`**
    - Updated: Comprehensive testing script for all functionalities
    - Added: Password reset testing instructions
    - Added: Detailed troubleshooting guide

## DEBUGGING STEPS

### 1. Test Backend Connection
```bash
curl -X GET "http://localhost:8080/api/auth/test-email-config"
```

### 2. Test Password Reset Email
```bash
curl -X POST "http://localhost:8080/api/auth/forgot-password" -H "Content-Type: application/json" -d "{\"email\":\"your-email@example.com\"}"
```

### 3. Test Admin Email Notification
```bash
curl -X POST "http://localhost:8080/api/auth/test-admin-notification-email"
```

### 4. Test Account Deletion (Backend)
```bash
curl -X POST "http://localhost:8080/api/auth/test-delete-account?email=test@example.com"
```

### 5. Test Frontend Flows
- **Password Reset**: Go to login page → "Forgot Password" → Enter email → Check email → Click reset link → Enter new password
- **Account Deletion**: Log in → Settings dropdown → "Delete Account" → Type "DELETE" → Confirm deletion

## DELETION PROCESS ORDER

The account deletion now follows this specific order to avoid database constraints:

1. **Delete Guest Bookings**: Remove all bookings made by the user as a guest on other properties
2. **Delete Owned Units**: Remove all accommodation units owned by the user (cascade deletes their bookings, photos, reviews)
3. **Delete Owner Applications**: Remove any owner application submitted by the user
4. **Delete Confirmation Tokens**: Remove email confirmation tokens
5. **Delete Password Reset Tokens**: Remove password reset tokens
6. **Delete User Account**: Finally delete the user (cascade deletes reviews written by the user)

## COMMON ISSUES AND SOLUTIONS

### Issue 1: "Failed to delete account. Please try again."
**Possible Causes:**
- Database foreign key constraint violations
- User has guest bookings that weren't deleted
- Network connectivity issues
- Authentication problems

**Debug Steps:**
1. Check backend logs for detailed error messages
2. Use the test endpoint: `POST /api/auth/test-delete-account?email=...`
3. Look for constraint violation messages in MySQL logs
4. Verify all related data is properly deleted

**Solution:**
- The deletion process now handles all related data in proper order
- Added findByGuestEmail to clean up guest bookings
- Enhanced logging shows exactly which step failed

### Issue 2: Password Reset Not Working
**Possible Causes:**
- Frontend using wrong API endpoint
- Email configuration issues
- Token validation problems

**Debug Steps:**
1. Test email configuration: `GET /api/auth/test-email-config`
2. Test forgot password: `POST /api/auth/forgot-password`
3. Check browser network tab for API call details
4. Check backend logs for email sending errors

**Solution:**
- Fixed frontend to use proper API instance
- Enhanced error handling and logging
- Verified email configuration

### Issue 3: Admin Not Receiving Owner Application Emails
**Possible Causes:**
- Missing admin notification call
- Email configuration issues
- SMTP authentication problems

**Debug Steps:**
1. Test admin notification: `POST /api/auth/test-admin-notification-email`
2. Check backend logs for email sending attempts
3. Verify admin email configuration

**Solution:**
- Fixed missing admin notification call in submitApplication
- Added proper email configuration
- Enhanced error handling and logging

## VERIFICATION CHECKLIST

### ✅ Password Reset
- [ ] User can request password reset from login page
- [ ] Reset email is sent and received
- [ ] Reset link works and leads to reset form
- [ ] New password can be set successfully
- [ ] User can log in with new password

### ✅ Owner Application Notifications
- [ ] Admin receives email when user applies to become owner
- [ ] Email contains applicant details and application message
- [ ] Backend logs show successful email sending
- [ ] Test endpoint works: `/api/auth/test-admin-notification-email`

### ✅ Account Deletion
- [ ] User can delete account from frontend settings
- [ ] All related data is removed in proper order
- [ ] User is automatically logged out after deletion
- [ ] No database constraint errors in backend logs
- [ ] Test endpoint works: `/api/auth/test-delete-account?email=...`

### ✅ Error Handling
- [ ] Clear, specific error messages shown to user
- [ ] Detailed error information logged in backend
- [ ] No generic "Internal Server Error" messages
- [ ] Proper HTTP status codes returned

## BACKEND LOG EXAMPLES

### Successful Account Deletion:
```
=== STARTING USER ACCOUNT DELETION ===
Email: john@example.com
✅ User found: John Doe (ID: 1)
📅 Deleting bookings made by user as guest...
Found 3 bookings to delete
✅ Deleted 3 bookings made by user as guest
🏠 Deleting accommodation units owned by user...
Found 2 accommodation units to delete
✅ Deleted 2 accommodation units
✅ USER ACCOUNT DELETED SUCCESSFULLY: john@example.com
```

### Successful Password Reset:
```
=== SENDING PASSWORD RESET EMAIL ===
To: john@example.com
Reset URL: http://localhost:3000/reset-password?token=abc123...
✅ PASSWORD RESET EMAIL SENT SUCCESSFULLY to john@example.com
```

### Error Example:
```
❌ ACCOUNT DELETION FAILED: could not execute statement; SQL [n/a]; constraint [fk_booking_guest_email]
📅 Deleting bookings made by user as guest...
Found 0 bookings to delete
❌ ERROR DURING USER DELETION: Database constraint violation
```

The enhanced logging will help you identify exactly where the process fails and why.
