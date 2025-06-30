@echo off
echo ============================================
echo Date Filtering Verification
echo ============================================
echo.

echo Testing backend health...
curl -s -o NUL -w "Backend Health: %%{http_code}\n" http://localhost:8080/api/units/debug/health

echo.
echo Testing units without date filter...
curl -s -w "No Filter Response: %%{http_code}\n" -o temp_response.json http://localhost:8080/api/units/public
set /p unitCount=<temp_response.json
echo Units returned (no filter): %unitCount%

echo.
echo Testing with valid date range...
curl -s -w "Date Filter Response: %%{http_code}\n" -o temp_response2.json "http://localhost:8080/api/units/public?checkIn=2025-07-01&checkOut=2025-07-05"
set /p dateFilterCount=<temp_response2.json
echo Units returned (with dates): %dateFilterCount%

echo.
echo Testing with invalid date range (past dates)...
curl -s -w "Past Date Response: %%{http_code}\n" -o temp_response3.json "http://localhost:8080/api/units/public?checkIn=2024-01-01&checkOut=2024-01-05"
set /p pastDateCount=<temp_response3.json
echo Units returned (past dates): %pastDateCount%

echo.
echo Testing with invalid date order...
curl -s -w "Invalid Order Response: %%{http_code}\n" -o temp_response4.json "http://localhost:8080/api/units/public?checkIn=2025-07-05&checkOut=2025-07-01"
set /p invalidOrderCount=<temp_response4.json
echo Units returned (invalid order): %invalidOrderCount%

echo.
echo Cleaning up temporary files...
del temp_response.json temp_response2.json temp_response3.json temp_response4.json

echo.
echo ============================================
echo Test completed! Check responses above.
echo ============================================
pause
