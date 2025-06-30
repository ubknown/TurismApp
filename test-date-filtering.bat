@echo off
echo ==========================================
echo DATE FILTERING AVAILABILITY TEST
echo ==========================================
echo.

REM Test if backend is running
echo Step 1: Test backend health...
curl -s http://localhost:8080/actuator/health
if %ERRORLEVEL% neq 0 (
    echo ❌ Backend is not running! Start it first with: mvn spring-boot:run
    pause
    exit /b 1
)
echo ✅ Backend is running
echo.

echo Step 2: Test basic units endpoint (no filters)...
curl -s "http://localhost:8080/api/units/public" | head -n 20
echo.

echo Step 3: Test date filtering...
echo Testing with check-in: 2025-07-01, check-out: 2025-07-05
curl -s "http://localhost:8080/api/units/public?checkIn=2025-07-01&checkOut=2025-07-05" | head -n 20
echo.

echo Step 4: Test date filtering with different dates...
echo Testing with check-in: 2025-08-15, check-out: 2025-08-20
curl -s "http://localhost:8080/api/units/public?checkIn=2025-08-15&checkOut=2025-08-20" | head -n 20
echo.

echo Step 5: Test invalid date range (check-out before check-in)...
echo Testing with check-in: 2025-07-05, check-out: 2025-07-01 (invalid)
curl -s "http://localhost:8080/api/units/public?checkIn=2025-07-05&checkOut=2025-07-01" | head -n 20
echo.

echo Step 6: Test past date (should return empty)...
echo Testing with check-in: 2025-01-01, check-out: 2025-01-05 (past dates)
curl -s "http://localhost:8080/api/units/public?checkIn=2025-01-01&checkOut=2025-01-05" | head -n 20
echo.

echo Step 7: Test combined filtering (county + dates)...
echo Testing with county=Brasov and dates 2025-07-01 to 2025-07-05
curl -s "http://localhost:8080/api/units/public?county=Brasov&checkIn=2025-07-01&checkOut=2025-07-05" | head -n 20
echo.

echo Test complete! Check the responses above.
echo.
pause
