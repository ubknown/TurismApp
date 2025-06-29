@echo off
echo ==============================================
echo Testing Admin Login with Plain Text Password
echo ==============================================
echo.

echo 1. Testing Admin Dashboard Login (AdminController)
echo -------------------------------------------------
curl -X POST "http://localhost:8080/api/admin/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}" ^
  --silent --show-error --write-out "HTTP Status: %%{http_code}\n"

echo.
echo 2. Testing Regular Auth Login (AuthController)
echo ----------------------------------------------
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}" ^
  --silent --show-error --write-out "HTTP Status: %%{http_code}\n"

echo.
echo ==============================================
echo Expected Results:
echo - Both should return HTTP Status: 200
echo - Response should contain admin user data
echo - No password encoding/hashing should occur
echo ==============================================
echo.
echo Setup Instructions:
echo 1. Run: create-admin-plaintext.sql in MySQL
echo 2. Start backend with: start-backend.bat
echo 3. Run this test script
echo.
pause
