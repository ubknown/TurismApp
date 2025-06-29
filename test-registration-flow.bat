@echo off
echo ===============================================
echo USER REGISTRATION FLOW - COMPREHENSIVE TEST
echo ===============================================
echo.

echo This script will test the complete user registration flow:
echo 1. Registration form submission
echo 2. Email confirmation system
echo 3. Success messages and redirects
echo 4. Email verification functionality
echo.

echo Step 1: Checking if backend is running...
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Backend is not running on http://localhost:8080
    echo Please start the backend first with: start-backend.bat
    echo.
    pause
    exit /b 1
)
echo ✅ Backend is running

echo.
echo Step 2: Checking if frontend is accessible...
curl -s http://localhost:5173 > nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  Frontend might not be running on http://localhost:5173
    echo Please start the frontend with: npm run dev
    echo.
)

echo.
echo Step 3: Testing registration endpoint...
echo.

set /p test_email="Enter test email (e.g., test@example.com): "
if "%test_email%"=="" set test_email=test@example.com

echo.
echo Testing GUEST registration with email: %test_email%
echo.

curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"%test_email%\",\"password\":\"TestPass123\",\"role\":\"GUEST\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo.
echo Testing OWNER registration with email: owner_%test_email%
echo.

curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"Owner\",\"email\":\"owner_%test_email%\",\"password\":\"TestPass123\",\"role\":\"OWNER\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo.
echo Step 4: Checking email service configuration...
echo.

curl -X GET http://localhost:8080/api/auth/test-email-config ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo.
echo Step 5: Testing duplicate registration...
echo.

curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"%test_email%\",\"password\":\"TestPass123\",\"role\":\"GUEST\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s

echo.
echo.
echo ===============================================
echo REGISTRATION FLOW TEST RESULTS
echo ===============================================
echo.

echo EXPECTED RESULTS:
echo.
echo 1. First registration (GUEST):
echo    - HTTP Status: 201 (Created)
echo    - Response should include confirmation message
echo    - User should receive confirmation email
echo.
echo 2. First registration (OWNER):
echo    - HTTP Status: 201 (Created)
echo    - Should create owner application automatically
echo    - User should receive confirmation email
echo.
echo 3. Email configuration test:
echo    - HTTP Status: 200
echo    - Should show email service status
echo.
echo 4. Duplicate registration:
echo    - HTTP Status: 200 (resend confirmation)
echo    - OR HTTP Status: 409 (conflict if already confirmed)
echo.

echo MANUAL VERIFICATION STEPS:
echo ===========================
echo.
echo 1. Check email inbox for confirmation emails
echo 2. Open frontend: http://localhost:5173/register
echo 3. Fill registration form and submit
echo 4. Verify success message appears
echo 5. Check redirect to login page with success banner
echo 6. Verify email confirmation link works
echo.

echo DATABASE VERIFICATION:
echo ======================
echo Run this SQL to check created users:
echo    USE turismdb;
echo    SELECT id, first_name, last_name, email, role, enabled, owner_status 
echo    FROM users WHERE email LIKE '%%test%%' ORDER BY created_at DESC;
echo.

echo Check owner applications:
echo    SELECT oa.id, u.email, oa.status, oa.submitted_at 
echo    FROM owner_applications oa 
echo    JOIN users u ON oa.user_id = u.id 
echo    WHERE u.email LIKE '%%test%%';
echo.

echo Check confirmation tokens:
echo    SELECT ct.token, u.email, ct.created_at, ct.expires_at, ct.confirmed_at
echo    FROM confirmation_tokens ct
echo    JOIN users u ON ct.user_id = u.id
echo    WHERE u.email LIKE '%%test%%';
echo.

pause
