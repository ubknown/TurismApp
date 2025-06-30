@echo off
setlocal enabledelayedexpanecho 5. Testing CORS preflight (OPTIONS request)...
curl -X OPTIONS "%BACKEND_URL%/api/units/public" -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: GET"
echo.
echo.

echo ========================================
echo RESULTS SUMMARY:
echo ========================================
echo - JSON responses = SUCCESS
echo - "403 Forbidden" = SECURITY PROBLEM  
echo - "Connection refused" = BACKEND NOT RUNNING
echo - Empty result with dates = FILTERING WORKING (no available units)
echo ========================================
echo.

echo Check the Spring Boot console for these log messages:
echo - 🔐 JWT Filter - Skipping public endpoint
echo - 🗓️ Availability filtering requested
echo - 🔍 Checking availability for unit
echo ========================================
pause================================
echo DEBUG: Spring Security and Date Filtering Test
echo ========================================
echo.

REM Set variables for testing (avoid date keyword conflicts)
set "BACKEND_URL=http://localhost:8080"
set "START_DATE=2025-07-01"
set "END_DATE=2025-07-10"

echo Testing Spring Security configuration and availability filtering...
echo Backend URL: %BACKEND_URL%
echo Start: %START_DATE%
echo End: %END_DATE%
echo.

echo 1. Testing backend health (should return 200)...
curl -s "%BACKEND_URL%/api/units/debug/health"
echo.
echo.

echo 2. Testing public units endpoint WITHOUT authentication (should NOT return 403)...
curl -s "%BACKEND_URL%/api/units/public"
echo.
echo.

echo 3. Testing public units endpoint WITH availability filter (should NOT return 403)...
curl -s "%BACKEND_URL%/api/units/public?checkIn=%START_DATE%&checkOut=%END_DATE%"
echo.
echo.

echo 4. Testing debug availability filter endpoint...
curl -s "%BACKEND_URL%/api/units/debug/date-filter?checkIn=%START_DATE%&checkOut=%END_DATE%"
echo.
echo.

echo 5. Testing CORS preflight (OPTIONS request)...
curl -v -X OPTIONS "%BACKEND_URL%/api/units/public" -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: GET" 2>&1 | findstr -i "HTTP\|Access-Control\|403"
echo.

echo ========================================
echo Security Status Check:
echo - 200 OK = Success
echo - 403 Forbidden = Spring Security blocking (PROBLEM)
echo - 401 Unauthorized = Missing authentication (expected for some endpoints)
echo - 404 Not Found = Endpoint doesn't exist
echo ========================================
echo.

echo Check the Spring Boot console for these messages:
echo - � JWT Filter - Skipping public endpoint
echo - 🗓️ Date filtering requested
echo - 🔍 Checking availability for unit
echo ========================================
pause
