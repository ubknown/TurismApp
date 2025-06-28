@echo off
echo.
echo ==========================================
echo Verifying Seeded Data via API
echo ==========================================
echo.

echo Testing debug endpoints to verify seeded data...
echo.

echo 1. Checking database health...
curl -X GET "http://localhost:8080/api/units/debug/health" -H "Content-Type: application/json"
echo.
echo.

echo 2. Checking total accommodation units count...
curl -X GET "http://localhost:8080/api/units/debug/count" -H "Content-Type: application/json"
echo.
echo.

echo 3. Fetching first 5 accommodation units...
curl -X GET "http://localhost:8080/api/units?page=0&size=5" -H "Content-Type: application/json"
echo.
echo.

echo 4. Testing units by location (Bucharest)...
curl -X GET "http://localhost:8080/api/units/search?location=Bucharest" -H "Content-Type: application/json"
echo.
echo.

echo ==========================================
echo Seeded Data Verification Complete
echo ==========================================
echo Expected results:
echo - Health check should return OK
echo - Count should be 19 units
echo - Units should have realistic Romanian names/locations
echo - Various property types and owners
echo.

pause
