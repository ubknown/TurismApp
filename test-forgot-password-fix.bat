@echo off
echo ========================================
echo TESTING FORGOT PASSWORD ENDPOINT FIX
echo ========================================
echo.

echo This script tests if the /api/auth/forgot-password endpoint is now publicly accessible.
echo.

echo Step 1: Check if backend is running...
netstat -an | findstr :8080 | findstr LISTENING
if %errorlevel% neq 0 (
    echo ERROR: Backend not running on port 8080
    echo Please start the backend first: mvnw.cmd spring-boot:run
    pause
    exit /b
)
echo ✅ Backend is running on port 8080
echo.

echo Step 2: Testing forgot-password endpoint (should return 200, not 403)...
echo.
echo Testing with valid email format:
curl -X POST http://localhost:8080/api/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s
echo.
echo.

echo Testing with different email:
curl -X POST http://localhost:8080/api/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"user@test.com\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s
echo.
echo.

echo Step 3: Testing other auth endpoints for verification...
echo.
echo Testing login endpoint:
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@test.com\",\"password\":\"wrong\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s
echo.
echo.

echo Testing register endpoint:
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"newuser@test.com\",\"password\":\"password123\",\"role\":\"GUEST\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s
echo.
echo.

echo Step 4: Testing protected endpoint (should still require auth)...
echo.
echo Testing protected endpoint without authentication (should get 401/403):
curl -X GET http://localhost:8080/api/units/debug/test ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -s
echo.

echo ========================================
echo INTERPRETATION OF RESULTS:
echo ========================================
echo.
echo Expected results:
echo ✅ forgot-password: HTTP Status 200 or 400 (not 403 Forbidden)
echo ✅ login: HTTP Status 400 or 401 (authentication failed, but endpoint accessible)
echo ✅ register: HTTP Status 201 or 400 (validation error, but endpoint accessible)
echo ✅ protected endpoint: HTTP Status 401 or 403 (properly secured)
echo.
echo ❌ If you see HTTP Status 403 for auth endpoints, the fix didn't work
echo ❌ If you see HTTP Status 200 for protected endpoints, security is broken
echo.
pause
