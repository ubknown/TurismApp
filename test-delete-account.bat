@echo off
echo ========================================
echo   ACCOUNT DELETION AND PASSWORD RESET TESTING
echo ========================================
echo.

echo 1. Testing backend connection...
curl -X GET "http://localhost:8080/api/auth/test-email-config" -H "Content-Type: application/json"
echo.
echo.

echo 2. Testing admin notification email...
curl -X POST "http://localhost:8080/api/auth/test-admin-notification-email" -H "Content-Type: application/json"
echo.
echo.

echo 3. Testing password reset email...
echo NOTE: Replace EMAIL_HERE with the email you want to test password reset for
curl -X POST "http://localhost:8080/api/auth/forgot-password" -H "Content-Type: application/json" -d "{\"email\":\"EMAIL_HERE\"}"
echo.
echo.

echo 4. Testing account deletion (replace EMAIL_HERE with actual email)...
echo NOTE: Replace EMAIL_HERE with the email of a test user you want to delete
curl -X POST "http://localhost:8080/api/auth/test-delete-account?email=EMAIL_HERE" -H "Content-Type: application/json"
echo.
echo.

echo ========================================
echo   MANUAL TESTING INSTRUCTIONS
echo ========================================
echo.
echo BACKEND SETUP:
echo 1. Start the backend: mvn spring-boot:run
echo 2. Check that email configuration is working (step 1 above)
echo.
echo PASSWORD RESET TESTING:
echo 1. Use step 3 above to test forgot password email sending
echo 2. Check your email for the reset link
echo 3. Click the reset link or manually navigate to the reset page
echo 4. Enter new password and confirm
echo 5. Try logging in with the new password
echo.
echo ACCOUNT DELETION TESTING:
echo 1. Create a test user through registration
echo 2. Log in with the test user
echo 3. Go to Settings (user avatar) and click "Delete Account"
echo 4. Type "DELETE" in the confirmation field
echo 5. Click "Delete Account" button
echo 6. Verify the account is deleted and user is logged out
echo 7. Try logging in with the deleted account (should fail)
echo.
echo BACKEND TEST (Alternative):
echo 1. Replace EMAIL_HERE in step 4 above with actual test email
echo 2. Run the test deletion command
echo 3. Check backend logs for detailed deletion process
echo.
echo TROUBLESHOOTING:
echo - Check backend console for detailed error logs
echo - Verify email configuration is correct
echo - Check database for remaining user data after deletion
echo - Test with different user types (guest, owner, admin)
echo.
echo If you see errors, check the backend logs for detailed information.
echo Look for messages starting with ❌, ✅, or 🔍 for important information.
echo.
pause
