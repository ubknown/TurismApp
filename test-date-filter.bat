@echo off
echo ====================================
echo Testing Date-Based Availability Filter
echo ====================================

echo.
echo 1. Testing backend debug endpoint...
curl -X GET "http://localhost:8080/api/units/debug/health" -H "Content-Type: application/json"

echo.
echo 2. Testing public units endpoint without date filter...
curl -X GET "http://localhost:8080/api/units/public" -H "Content-Type: application/json"

echo.
echo 3. Testing public units with date filter (today + 1 week)...
for /f "tokens=1-3 delims=/" %%a in ('date /t') do (
    set today=%%c-%%a-%%b
)
curl -X GET "http://localhost:8080/api/units/public?checkIn=2025-07-01&checkOut=2025-07-05" -H "Content-Type: application/json"

echo.
echo 4. Testing invalid date range (past dates)...
curl -X GET "http://localhost:8080/api/units/public?checkIn=2024-01-01&checkOut=2024-01-05" -H "Content-Type: application/json"

echo.
echo 5. Testing invalid date range (checkout before checkin)...
curl -X GET "http://localhost:8080/api/units/public?checkIn=2025-07-05&checkOut=2025-07-01" -H "Content-Type: application/json"

echo.
echo ====================================
echo Date Filter Testing Complete
echo ====================================
pause
