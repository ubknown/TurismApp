@echo off
echo ============================================================
echo COMPREHENSIVE USER REGISTRATION FLOW TEST
echo ============================================================
echo.

echo This script will test the complete user registration workflow:
echo 1. Register a new account (normal user or owner)
echo 2. Check confirmation message display
echo 3. Verify email sending functionality
echo 4. Confirm redirect to login page with instructions
echo.

set /p continue="Continue with testing? (y/n): "
if /i not "%continue%"=="y" (
    echo Test cancelled.
    pause
    exit /b 0
)

echo.
echo ============================================================
echo STEP 1: BACKEND HEALTH CHECK
echo ============================================================
echo.

echo Checking if backend is running...
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Backend is not running on http://localhost:8080
    echo Please start the backend with: start-backend.bat
    echo.
    pause
    exit /b 1
)
echo ✅ Backend is running

echo.
echo ============================================================
echo STEP 2: FRONTEND HEALTH CHECK
echo ============================================================
echo.

echo Checking if frontend is accessible...
curl -s http://localhost:5173 > nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Frontend is not accessible on http://localhost:5173
    echo Please start the frontend with: npm run dev
    echo.
    pause
    exit /b 1
)
echo ✅ Frontend is accessible

echo.
echo ============================================================
echo STEP 3: TEST USER REGISTRATION - GUEST ACCOUNT
echo ============================================================
echo.

set TEST_EMAIL=test.user.%RANDOM%@example.com
echo Testing registration with email: %TEST_EMAIL%
echo.

curl -X POST "http://localhost:8080/api/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"%TEST_EMAIL%\",\"password\":\"TestPass123\",\"role\":\"GUEST\"}" ^
  -w "HTTP Status: %%{http_code}\n" ^
  -o registration_response.json

echo.
echo Registration response saved to registration_response.json
echo.
type registration_response.json
echo.

echo ============================================================
echo STEP 4: TEST USER REGISTRATION - OWNER ACCOUNT
echo ============================================================
echo.

set OWNER_EMAIL=test.owner.%RANDOM%@example.com
echo Testing owner registration with email: %OWNER_EMAIL%
echo.

curl -X POST "http://localhost:8080/api/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"Owner\",\"email\":\"%OWNER_EMAIL%\",\"password\":\"OwnerPass123\",\"role\":\"OWNER\"}" ^
  -w "HTTP Status: %%{http_code}\n" ^
  -o owner_registration_response.json

echo.
echo Owner registration response saved to owner_registration_response.json
echo.
type owner_registration_response.json
echo.

echo ============================================================
echo STEP 5: EMAIL CONFIGURATION CHECK
echo ============================================================
echo.

echo Testing email service configuration...
curl -X GET "http://localhost:8080/api/auth/test-email-config" ^
  -H "Content-Type: application/json" ^
  -w "HTTP Status: %%{http_code}\n"

echo.
echo ============================================================
echo STEP 6: MANUAL EMAIL VERIFICATION TEST
echo ============================================================
echo.

echo To manually test email verification:
echo.
echo 1. Check your email inbox for confirmation emails sent to:
echo    - %TEST_EMAIL%
echo    - %OWNER_EMAIL%
echo.
echo 2. If emails are not received, check backend logs for:
echo    - "CONFIRMATION EMAIL SENT SUCCESSFULLY"
echo    - "MANUAL CONFIRMATION URL" (use this if email fails)
echo.
echo 3. Click the confirmation link in the email or use the manual URL
echo.

echo ============================================================
echo STEP 7: FRONTEND REGISTRATION FLOW TEST
echo ============================================================
echo.

echo Please manually test the frontend registration flow:
echo.
echo 1. Open: http://localhost:5173/register
echo 2. Fill out the registration form with valid data
echo 3. Submit the form
echo.
echo Expected behavior:
echo   ✅ Success message appears: "Registration successful! A confirmation email has been sent."
echo   ✅ Toast notification shows: "Registration Successful - Redirecting to login page..."
echo   ✅ Automatic redirect to login page after 1 second
echo   ✅ Success banner appears on login page with personalized message
echo   ✅ Banner shows user email and role
echo   ✅ Banner auto-hides after 6 seconds
echo   ✅ Manual close button works on banner
echo.

echo ============================================================
echo STEP 8: LOGIN PAGE VERIFICATION
echo ============================================================
echo.

echo Please verify the login page shows:
echo.
echo 1. Success banner with message like:
echo    "Please check your email (user@example.com) to activate your Guest account before logging in."
echo.
echo 2. Banner should have:
echo   ✅ Green checkmark icon
echo   ✅ Email icon
echo   ✅ Progress bar countdown
echo   ✅ Violet/indigo glassmorphism design
echo   ✅ Close button (X)
echo.

echo ============================================================
echo STEP 9: EMAIL CONFIRMATION FLOW TEST
echo ============================================================
echo.

echo To test the complete email confirmation:
echo.
echo 1. Check email for confirmation link
echo 2. Click the link (should redirect to: http://localhost:5173/email-confirmed?success=true)
echo 3. Verify confirmation page shows Romanian success message:
echo    "Înregistrarea a fost făcută cu succes!"
echo.
echo 4. Try logging in with confirmed account
echo.

echo ============================================================
echo STEP 10: ERROR TESTING
echo ============================================================
echo.

echo Testing duplicate email registration...
echo.

curl -X POST "http://localhost:8080/api/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Duplicate\",\"lastName\":\"User\",\"email\":\"%TEST_EMAIL%\",\"password\":\"TestPass123\",\"role\":\"GUEST\"}" ^
  -w "HTTP Status: %%{http_code}\n"

echo.
echo Expected: Should show message about resending confirmation email
echo.

echo ============================================================
echo REGISTRATION FLOW TEST COMPLETE
echo ============================================================
echo.

echo ✅ WHAT WAS TESTED:
echo   - Backend registration endpoints (/api/auth/register)
echo   - Email service configuration
echo   - JSON response format
echo   - Duplicate email handling
echo   - HTTP status codes
echo.

echo 🔍 WHAT TO VERIFY MANUALLY:
echo   - Frontend registration form functionality
echo   - Success message display on registration page
echo   - Toast notifications
echo   - Redirect to login page
echo   - Success banner on login page
echo   - Email confirmation links
echo   - Email confirmation page (Romanian messages)
echo.

echo 📧 EMAIL VERIFICATION:
echo   - Check inbox for confirmation emails
echo   - Test clicking confirmation links
echo   - Verify email-confirmed page displays properly
echo.

echo 🐛 IF ISSUES FOUND:
echo   - Check backend logs in terminal running spring-boot:run
echo   - Check browser console for JavaScript errors
echo   - Verify email configuration in application.properties
echo   - Test with different browsers
echo.

echo 📋 FILES TO CHECK:
echo   - Backend logs for email sending attempts
echo   - registration_response.json (guest registration)
echo   - owner_registration_response.json (owner registration)
echo   - Browser DevTools Console for frontend errors
echo.

del registration_response.json > nul 2>&1
del owner_registration_response.json > nul 2>&1

echo.
pause
