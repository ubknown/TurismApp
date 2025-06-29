@echo off
echo ===============================================
echo ADMIN LOGIN 403 FIX - COMPREHENSIVE TEST
echo ===============================================
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
echo Step 2: Verifying admin user exists in database...
echo Please manually check your database for the admin user with:
echo SELECT * FROM users WHERE role = 'ADMIN';
echo.
pause

echo.
echo Step 3: Testing admin login endpoint...
echo Making POST request to /api/admin/login...
echo.

curl -X POST http://localhost:8080/api/admin/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@turismapp.com\",\"password\":\"admin123\"}" ^
  -w "HTTP Status: %%{http_code}\n" ^
  -s

echo.
echo.
echo Step 4: Testing with verbose output...
echo.

curl -X POST http://localhost:8080/api/admin/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@turismapp.com\",\"password\":\"admin123\"}" ^
  -v

echo.
echo.
echo ===============================================
echo TROUBLESHOOTING GUIDE:
echo ===============================================
echo If you still get 403 Forbidden:
echo 1. Restart the backend to apply SecurityConfig changes
echo 2. Check backend logs for authentication errors
echo 3. Verify admin user exists with correct email/password
echo 4. Ensure database connection is working
echo.
echo If you get 401 Unauthorized:
echo 1. Check if admin user exists in database
echo 2. Verify email and password are correct
echo 3. Check if password is stored as plain text
echo.
echo Expected response for successful login:
echo {
echo   "message": "Admin login successful",
echo   "token": "jwt-token-here",
echo   "user": {
echo     "id": 1,
echo     "email": "admin@turismapp.com",
echo     "firstName": "Admin",
echo     "lastName": "User",
echo     "role": "ADMIN"
echo   }
echo }
echo.
pause
