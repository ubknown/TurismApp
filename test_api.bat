@echo off
echo ========================================
echo Tourism App Backend API Testing Script
echo ========================================
echo.

REM Test 1: Health Check
echo [TEST 1] Testing backend connection...
curl -s -o nul -w "Backend Status: %%{http_code}\n" http://localhost:8080/api/units/public
echo.

REM Test 2: Registration
echo [TEST 2] Testing user registration...
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"password\":\"password123\",\"phone\":\"+1234567890\",\"address\":\"123 Test St\"}" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

REM Test 3: Login  
echo [TEST 3] Testing user login...
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"password123\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" > login_response.json
echo.
echo Login response saved to login_response.json
echo Please copy the JWT token from the response for next tests.
echo.

REM Test 4: Public Units
echo [TEST 4] Testing public units endpoint...
curl -X GET "http://localhost:8080/api/units/public?search=mountain" ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP Status: %%{http_code}\n"
echo.
echo.

echo ========================================
echo Manual Testing Instructions:
echo ========================================
echo 1. Copy the JWT token from login_response.json
echo 2. Replace YOUR_JWT_TOKEN in the commands below:
echo.
echo Test My Units:
echo curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/units/my-units
echo.
echo Test Dashboard Stats:
echo curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/profit/stats
echo.
echo Test Recent Bookings:
echo curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/bookings/recent
echo.
echo ========================================

pause
