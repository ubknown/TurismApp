@echo off
echo ========================================
echo COMPREHENSIVE ADMIN LOGIN DEBUG
echo ========================================
echo.
echo This script will test ALL aspects of authentication
echo.

REM Check if backend is running
echo Testing if backend is running...
curl -s "http://localhost:8080/api/auth/test-email-config" > nul
if errorlevel 1 (
    echo ❌ ERROR: Backend is not running on port 8080
    echo Please start the backend first with: mvnw spring-boot:run
    pause
    exit /b 1
) else (
    echo ✅ Backend is running
)

echo.
echo ========================================
echo STEP 1: PASSWORD HASH TEST
echo ========================================
echo.

echo Testing password hash for 'admin123':
curl -s "http://localhost:8080/api/auth/test-password-hash?password=admin123"
echo.
echo.

echo ========================================
echo STEP 2: USER DETAILS DEBUG
echo ========================================
echo.

echo Testing user details loading:
curl -s "http://localhost:8080/api/auth/debug-user-details?email=admin@tourism.com"
echo.
echo.

echo ========================================
echo STEP 3: AUTHENTICATION COMPONENTS TEST
echo ========================================
echo.

echo Testing all authentication components:
curl -X POST "http://localhost:8080/api/auth/debug-authenticate" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}"
echo.
echo.

echo ========================================
echo STEP 4: ACTUAL LOGIN TEST
echo ========================================
echo.

echo Testing actual login:
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -v
echo.
echo.

echo ========================================
echo STEP 5: POSTMAN/CURL TEST COMMANDS
echo ========================================
echo.
echo You can also test manually with these commands:
echo.
echo 1. Password Hash Test:
echo curl "http://localhost:8080/api/auth/test-password-hash?password=admin123"
echo.
echo 2. User Details Test:
echo curl "http://localhost:8080/api/auth/debug-user-details?email=admin@tourism.com"
echo.
echo 3. Authentication Test:
echo curl -X POST "http://localhost:8080/api/auth/debug-authenticate" \
echo   -H "Content-Type: application/json" \
echo   -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}"
echo.
echo 4. Actual Login:
echo curl -X POST "http://localhost:8080/api/auth/login" \
echo   -H "Content-Type: application/json" \
echo   -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}"
echo.

echo ========================================
echo TROUBLESHOOTING CHECKLIST
echo ========================================
echo.
echo ✅ Check Step 1: passwordMatches should be true
echo ✅ Check Step 2: userExists, userEnabled should be true
echo ✅ Check Step 3: readyForAuthentication should be true
echo ✅ Check Step 4: Should return HTTP 200 with token
echo.
echo If any step fails, check the backend console for detailed logs!
echo.

pause
