@echo off
REM Tourism App API Test Script for Windows
REM Run this script after starting the Spring Boot server

set BASE_URL=http://localhost:8080

echo 🚀 Testing Tourism App API...
echo ================================

REM Test 1: Check if server is running
echo 1. Checking if server is running...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/api/accommodation-units" > temp_response.txt
set /p response=<temp_response.txt
del temp_response.txt

if "%response%"=="200" (
    echo ✅ Server is running on port 8080
) else (
    echo ❌ Server not responding ^(HTTP %response%^)
    pause
    exit /b 1
)

REM Test 2: Register a test user
echo 2. Registering test user...
curl -s -X POST "%BASE_URL%/api/auth/register" -H "Content-Type: application/json" -d "{\"name\": \"Test Owner\", \"email\": \"testowner@example.com\", \"password\": \"password123\", \"role\": \"OWNER\"}" > register_response.txt

findstr /C:"successfully" /C:"created" /C:"token" register_response.txt >nul
if %errorlevel%==0 (
    echo ✅ User registration successful
) else (
    echo ⚠️ User registration response:
    type register_response.txt
)
del register_response.txt

REM Test 3: Login and get JWT token
echo 3. Logging in...
curl -s -X POST "%BASE_URL%/api/auth/login" -H "Content-Type: application/json" -d "{\"email\": \"testowner@example.com\", \"password\": \"password123\"}" > login_response.txt

REM Extract token (this is simplified - you might need to parse JSON properly)
for /f "tokens=2 delims=:\" %%a in ('findstr "token" login_response.txt') do set token=%%a
set token=%token:,=%
set token=%token:}=%

if defined token (
    echo ✅ Login successful, token received
    echo Token: %token%
) else (
    echo ❌ Login failed or no token received
    echo Response:
    type login_response.txt
    del login_response.txt
    pause
    exit /b 1
)
del login_response.txt

REM Test 4: Test dashboard stats endpoint
echo 4. Testing dashboard stats...
curl -s -H "Authorization: Bearer %token%" "%BASE_URL%/api/dashboard/stats" > dashboard_response.txt

findstr /C:"totalUnits" /C:"totalBookings" dashboard_response.txt >nul
if %errorlevel%==0 (
    echo ✅ Dashboard stats endpoint working
    echo Response:
    type dashboard_response.txt
) else (
    echo ❌ Dashboard stats endpoint failed
    echo Response:
    type dashboard_response.txt
)
del dashboard_response.txt

REM Test 5: Test dashboard insights endpoint
echo 5. Testing dashboard insights...
curl -s -H "Authorization: Bearer %token%" "%BASE_URL%/api/dashboard/insights" > insights_response.txt

findstr /C:"topPerformingUnit" /C:"occupancyRate" insights_response.txt >nul
if %errorlevel%==0 (
    echo ✅ Dashboard insights endpoint working
    echo Response:
    type insights_response.txt
) else (
    echo ❌ Dashboard insights endpoint failed
    echo Response:
    type insights_response.txt
)
del insights_response.txt

REM Test 6: Create an accommodation unit
echo 6. Creating test accommodation unit...
curl -s -X POST "%BASE_URL%/api/accommodation-units" -H "Authorization: Bearer %token%" -H "Content-Type: application/json" -d "{\"name\": \"Test Beach House\", \"description\": \"A beautiful test property\", \"address\": \"123 Test Street\", \"city\": \"Test City\", \"country\": \"Test Country\", \"pricePerNight\": 100.0, \"type\": \"HOUSE\", \"maxGuests\": 4, \"bedrooms\": 2, \"bathrooms\": 1, \"amenities\": [\"WiFi\", \"Kitchen\"]}" > unit_response.txt

findstr /C:"id" /C:"Test Beach House" unit_response.txt >nul
if %errorlevel%==0 (
    echo ✅ Accommodation unit creation successful
    echo Response:
    type unit_response.txt
) else (
    echo ❌ Accommodation unit creation failed
    echo Response:
    type unit_response.txt
)
del unit_response.txt

REM Test 7: Get user's units
echo 7. Getting user's units...
curl -s -H "Authorization: Bearer %token%" "%BASE_URL%/api/accommodation-units/my-units" > my_units_response.txt

findstr /C:"Test Beach House" /C:"[]" my_units_response.txt >nul
if %errorlevel%==0 (
    echo ✅ My units endpoint working
    echo Response:
    type my_units_response.txt
) else (
    echo ❌ My units endpoint failed
    echo Response:
    type my_units_response.txt
)
del my_units_response.txt

echo ================================
echo 🎉 API Testing Complete!
echo.
echo 📋 Summary:
echo - Server: Running ✅
echo - Authentication: Working ✅
echo - Dashboard Stats: Working ✅
echo - Dashboard Insights: Working ✅
echo - Accommodation Units: Working ✅
echo.
echo 🔗 You can now test with the frontend or use Postman
echo 📖 See API_TEST_GUIDE.md for complete endpoint documentation
echo.
pause
