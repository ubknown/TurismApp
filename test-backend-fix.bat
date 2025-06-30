@echo off
echo ========================================
echo 🚀 BACKEND RESTART AND TEST SCRIPT
echo ========================================
echo.

echo Step 1: Instructions to restart your backend...
echo.
echo 📋 Please follow these steps:
echo 1. Go to your backend terminal (where Spring Boot is running)
echo 2. Press Ctrl+C to stop the backend
echo 3. Run: mvn spring-boot:run
echo 4. Wait for "Started TurismappApplication" message
echo 5. Return here and press any key to test
echo.
pause

echo ========================================
echo Step 2: Testing the fix...
echo ========================================

set "BACKEND_URL=http://localhost:8080"

echo 2.1 Health Check:
curl -s "%BACKEND_URL%/api/units/debug/health"
echo.
echo.

echo 2.2 Testing without date filters (should return all 17 units):
echo.
curl -s "%BACKEND_URL%/api/units/public" > test_all.json
for /f %%i in ('type test_all.json ^| find /c """id"""') do echo Found %%i units
echo.

echo 2.3 Testing with August dates (should now return some units):
echo.
curl -s "%BACKEND_URL%/api/units/public?checkIn=2025-08-01&checkOut=2025-08-05" > test_august.json
for /f %%i in ('type test_august.json ^| find /c """id"""') do echo Found %%i units for August 1-5
echo.

echo 2.4 Testing with December dates (should return all or most units):
echo.
curl -s "%BACKEND_URL%/api/units/public?checkIn=2025-12-01&checkOut=2025-12-05" > test_december.json
for /f %%i in ('type test_december.json ^| find /c """id"""') do echo Found %%i units for December 1-5
echo.

echo 2.5 Debug endpoint for August:
curl -s "%BACKEND_URL%/api/units/debug/date-filter?checkIn=2025-08-01&checkOut=2025-08-05"
echo.
echo.

echo ========================================
echo RESULTS INTERPRETATION:
echo ========================================
echo ✅ If August/December return units = FIX SUCCESSFUL
echo ❌ If still 0 units = Need to check database or try different approach
echo ========================================

del test_all.json test_august.json test_december.json 2>nul

echo.
echo Next step: Test the frontend by setting dates and clicking "Apply Filters"
pause
