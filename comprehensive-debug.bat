@echo off
setlocal enabledelayedexpansion
echo ========================================
echo COMPREHENSIVE DEBUG: Date Filtering Issue
echo ========================================
echo.

REM Set variables for testing
set "BACKEND_URL=http://localhost:8080"
set "TODAY_DATE=2025-06-29"
set "FUTURE_DATE1=2025-08-01"
set "FUTURE_DATE2=2025-08-10"
set "FAR_FUTURE1=2025-12-01"
set "FAR_FUTURE2=2025-12-10"

echo Step 1: Testing basic connectivity...
echo Backend URL: %BACKEND_URL%
echo.

echo 1.1 Health Check:
curl -s "%BACKEND_URL%/api/units/debug/health"
echo.
echo.

echo 1.2 Total units available (no filters):
curl -s "%BACKEND_URL%/api/units/public" | find /c "\"id\""
echo.

echo ========================================
echo Step 2: Testing date filtering with different date ranges...
echo ========================================

echo 2.1 Testing with July dates (original problem dates):
echo URL: %BACKEND_URL%/api/units/public?checkIn=2025-07-01^&checkOut=2025-07-10
curl -s "%BACKEND_URL%/api/units/public?checkIn=2025-07-01&checkOut=2025-07-10" | find /c "\"id\""
echo.

echo 2.2 Testing with August dates:
echo URL: %BACKEND_URL%/api/units/public?checkIn=%FUTURE_DATE1%^&checkOut=%FUTURE_DATE2%
curl -s "%BACKEND_URL%/api/units/public?checkIn=%FUTURE_DATE1%&checkOut=%FUTURE_DATE2%" | find /c "\"id\""
echo.

echo 2.3 Testing with December dates (far future):
echo URL: %BACKEND_URL%/api/units/public?checkIn=%FAR_FUTURE1%^&checkOut=%FAR_FUTURE2%
curl -s "%BACKEND_URL%/api/units/public?checkIn=%FAR_FUTURE1%&checkOut=%FAR_FUTURE2%" | find /c "\"id\""
echo.

echo ========================================
echo Step 3: Testing debug endpoint for detailed information...
echo ========================================

echo 3.1 Debug info for July dates:
curl -s "%BACKEND_URL%/api/units/debug/date-filter?checkIn=2025-07-01&checkOut=2025-07-10"
echo.
echo.

echo 3.2 Debug info for August dates:
curl -s "%BACKEND_URL%/api/units/debug/date-filter?checkIn=%FUTURE_DATE1%&checkOut=%FUTURE_DATE2%"
echo.
echo.

echo 3.3 Debug info for December dates:
curl -s "%BACKEND_URL%/api/units/debug/date-filter?checkIn=%FAR_FUTURE1%&checkOut=%FAR_FUTURE2%"
echo.
echo.

echo ========================================
echo Step 4: Testing edge cases...
echo ========================================

echo 4.1 Testing with only checkIn parameter:
curl -s "%BACKEND_URL%/api/units/public?checkIn=2025-08-01" | find /c "\"id\""
echo.

echo 4.2 Testing with only checkOut parameter:
curl -s "%BACKEND_URL%/api/units/public?checkOut=2025-08-10" | find /c "\"id\""
echo.

echo 4.3 Testing with invalid date format:
curl -s "%BACKEND_URL%/api/units/public?checkIn=invalid&checkOut=2025-08-10" | find /c "\"id\""
echo.

echo ========================================
echo ANALYSIS GUIDE:
echo ========================================
echo - If all date ranges return 0 units = Problem with database or filtering logic
echo - If some date ranges return units = Filtering is working, just no availability
echo - If debug endpoint shows success=false = Backend error
echo - If debug endpoint shows different totalActiveUnits vs availableUnitsAfterFilter = Filtering is working
echo ========================================
echo.

echo Instructions for next steps:
echo 1. Look at the numbers above
echo 2. Check if ANY date range returns units
echo 3. If all return 0, we need to check your database
echo 4. If some return units, the filtering is working correctly
echo ========================================
pause
