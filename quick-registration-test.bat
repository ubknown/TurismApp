@echo off
echo ===============================================
echo REGISTRATION FLOW - QUICK VERIFICATION TEST
echo ===============================================
echo.

echo Testing the restored registration flow behavior:
echo ✓ Email verification message (3 seconds)
echo ✓ Automatic redirect to login
echo ✓ Success banner with verification instructions
echo.

echo Step 1: Testing registration endpoint...
echo ========================================
echo.

set /p test_email="Enter test email (or press Enter for default): "
if "%test_email%"=="" set test_email=testuser@example.com

echo.
echo Testing registration with: %test_email%
echo.

curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"%test_email%\",\"password\":\"TestPass123\",\"role\":\"GUEST\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo.
echo ===============================================
echo FRONTEND TESTING INSTRUCTIONS
echo ===============================================
echo.

echo Now test the frontend registration flow:
echo.
echo 1. Open: http://localhost:5173/register
echo 2. Fill the registration form
echo 3. Click "Create Account"
echo.
echo EXPECTED BEHAVIOR:
echo ✓ Success message appears: "Please verify your email to activate your account"
echo ✓ Toast notification shows with email verification details
echo ✓ Message displays for 3 FULL SECONDS (time to read)
echo ✓ Automatic redirect to login page
echo ✓ Success banner on login page with verification instructions
echo.

echo VERIFICATION CHECKLIST:
echo [ ] Success message displays for 3 seconds
echo [ ] Message says "Please verify your email to activate..."
echo [ ] Toast shows user's email address
echo [ ] Automatic redirect after 3 seconds
echo [ ] Success banner appears on login page
echo [ ] Banner emphasizes email verification requirement
echo [ ] Email confirmation sent to inbox
echo.

echo If all items work correctly, the registration flow restoration is successful!
echo.

pause
