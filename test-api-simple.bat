@echo off
REM Simple test script for Spring Security and Date Filtering
REM This script avoids potential conflicts with Windows commands

echo ======== Testing Spring Security and Date Filtering ========
echo.

REM Set test variables with quotes to avoid conflicts
set "BASE_URL=http://localhost:8080"
set "DATE1=2025-07-01"
set "DATE2=2025-07-10"

echo Testing Backend URL: %BASE_URL%
echo Testing Dates: %DATE1% to %DATE2%
echo.

echo 1. Backend Health Check...
curl -s "%BASE_URL%/api/units/debug/health"
echo.
echo.

echo 2. Public Units Endpoint (No Auth Required)...
curl -s "%BASE_URL%/api/units/public"
echo.
echo.

echo 3. Public Units with Date Filter...
curl -s "%BASE_URL%/api/units/public?checkIn=%DATE1%&checkOut=%DATE2%"
echo.
echo.

echo 4. Debug Date Filter Endpoint...
curl -s "%BASE_URL%/api/units/debug/date-filter?checkIn=%DATE1%&checkOut=%DATE2%"
echo.
echo.

echo 5. CORS Test (OPTIONS request)...
curl -X OPTIONS "%BASE_URL%/api/units/public" -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: GET"
echo.
echo.

echo ========================================
echo RESULTS INTERPRETATION:
echo ========================================
echo - If you see JSON responses = SUCCESS
echo - If you see "403 Forbidden" = SECURITY PROBLEM
echo - If you see "Connection refused" = BACKEND NOT RUNNING
echo ========================================
pause
