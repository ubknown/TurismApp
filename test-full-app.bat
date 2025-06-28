@echo off
REM Complete Tourism App Testing Script
REM Tests both backend API and frontend integration

echo 🏖️ Tourism App Complete Testing Suite
echo ========================================
echo.

set BACKEND_URL=http://localhost:8080
set FRONTEND_URL=http://localhost:5173

echo 📋 Pre-Test Checklist:
echo [ ] Spring Boot backend running on port 8080
echo [ ] React frontend running on port 5173  
echo [ ] MySQL database running with 'turismdb'
echo.
pause

echo 🔍 Testing Backend API...
echo --------------------------

REM Test backend health
curl -s -o nul -w "%%{http_code}" "%BACKEND_URL%/api/accommodation-units" > backend_status.txt
set /p backend_status=<backend_status.txt
del backend_status.txt

if "%backend_status%"=="200" (
    echo ✅ Backend is running on port 8080
) else (
    echo ❌ Backend not responding ^(HTTP %backend_status%^)
    echo Make sure Spring Boot is running with: mvn spring-boot:run
    pause
    exit /b 1
)

REM Test frontend health
curl -s -o nul -w "%%{http_code}" "%FRONTEND_URL%" > frontend_status.txt 2>nul
set /p frontend_status=<frontend_status.txt 2>nul
del frontend_status.txt 2>nul

if "%frontend_status%"=="200" (
    echo ✅ Frontend is running on port 5173
) else (
    echo ⚠️  Frontend not detected on port 5173
    echo Start it with: npm run dev
)

echo.
echo 🧪 Running API Integration Tests...
echo ----------------------------------

REM Register test user
echo Registering test user...
curl -s -X POST "%BACKEND_URL%/api/auth/register" ^
    -H "Content-Type: application/json" ^
    -d "{\"name\": \"Test Owner\", \"email\": \"testowner123@example.com\", \"password\": \"password123\", \"role\": \"OWNER\"}" > register_result.txt

findstr /C:"successfully\|created\|token" register_result.txt >nul
if %errorlevel%==0 (
    echo ✅ User registration successful
) else (
    echo ⚠️  User registration: ^(user may already exist^)
)
del register_result.txt

REM Login and get token
echo Logging in...
curl -s -X POST "%BACKEND_URL%/api/auth/login" ^
    -H "Content-Type: application/json" ^
    -d "{\"email\": \"testowner123@example.com\", \"password\": \"password123\"}" > login_result.txt

REM Extract token
for /f "tokens=2 delims=:\" %%a in ('findstr "token" login_result.txt') do set token=%%a
set token=%token:,=%
set token=%token:}=%
del login_result.txt

if defined token (
    echo ✅ Login successful
) else (
    echo ❌ Login failed
    pause
    exit /b 1
)

REM Test dashboard endpoints
echo Testing dashboard stats...
curl -s -H "Authorization: Bearer %token%" "%BACKEND_URL%/api/dashboard/stats" > dashboard_stats.txt
findstr /C:"totalUnits\|totalBookings" dashboard_stats.txt >nul
if %errorlevel%==0 (
    echo ✅ Dashboard stats endpoint working
) else (
    echo ❌ Dashboard stats failed
)

echo Testing dashboard insights...
curl -s -H "Authorization: Bearer %token%" "%BACKEND_URL%/api/dashboard/insights" > dashboard_insights.txt
findstr /C:"occupancyRate\|revenueGrowth" dashboard_insights.txt >nul
if %errorlevel%==0 (
    echo ✅ Dashboard insights endpoint working
) else (
    echo ❌ Dashboard insights failed
)

REM Create test accommodation unit
echo Creating test accommodation unit...
curl -s -X POST "%BACKEND_URL%/api/accommodation-units" ^
    -H "Authorization: Bearer %token%" ^
    -H "Content-Type: application/json" ^
    -d "{\"name\": \"Test Beach Villa\", \"description\": \"A beautiful test property for testing\", \"address\": \"123 Test Beach Rd\", \"city\": \"Miami\", \"country\": \"USA\", \"pricePerNight\": 200.0, \"type\": \"VILLA\", \"maxGuests\": 6, \"bedrooms\": 3, \"bathrooms\": 2, \"amenities\": [\"WiFi\", \"Pool\", \"Beach Access\", \"Kitchen\"]}" > unit_result.txt

findstr /C:"id\|Test Beach Villa" unit_result.txt >nul
if %errorlevel%==0 (
    echo ✅ Accommodation unit created successfully
    REM Extract unit ID for booking
    for /f "tokens=2 delims=:" %%a in ('findstr "\"id\"" unit_result.txt') do set unit_id=%%a
    set unit_id=%unit_id:,=%
    set unit_id=%unit_id: =%
) else (
    echo ❌ Failed to create accommodation unit
    set unit_id=1
)
del unit_result.txt

REM Create test booking
echo Creating test booking...
curl -s -X POST "%BACKEND_URL%/api/bookings" ^
    -H "Authorization: Bearer %token%" ^
    -H "Content-Type: application/json" ^
    -d "{\"accommodationUnitId\": %unit_id%, \"checkInDate\": \"2025-08-01\", \"checkOutDate\": \"2025-08-05\", \"guestName\": \"John Test\", \"guestEmail\": \"john.test@example.com\", \"totalPrice\": 800.0}" > booking_result.txt

findstr /C:"id\|John Test" booking_result.txt >nul
if %errorlevel%==0 (
    echo ✅ Booking created successfully
) else (
    echo ❌ Failed to create booking
)
del booking_result.txt

REM Re-test dashboard with data
echo Re-testing dashboard with created data...
curl -s -H "Authorization: Bearer %token%" "%BACKEND_URL%/api/dashboard/stats" > final_stats.txt
echo Final Dashboard Stats:
type final_stats.txt
del final_stats.txt

REM Clean up temp files
del dashboard_stats.txt 2>nul
del dashboard_insights.txt 2>nul

echo.
echo 🎉 API Testing Complete!
echo ========================

echo.
echo 🌐 Frontend Testing Instructions:
echo --------------------------------
echo 1. Open browser to: %FRONTEND_URL%
echo 2. Register/Login with: testowner123@example.com / password123
echo 3. Check dashboard shows: 1 Unit, 1 Booking, $800 Revenue
echo 4. Verify all stat cards display data
echo 5. Test navigation between pages
echo 6. Check responsive design on mobile

echo.
echo 📊 Expected Dashboard Results:
echo - Total Units: 1
echo - Total Bookings: 1  
echo - Total Revenue: $800
echo - Recent Bookings: John Test booking
echo - Occupancy Rate: calculated value
echo - Average Rating: 0 ^(no reviews yet^)

echo.
echo 🔗 Quick Links:
echo Frontend: %FRONTEND_URL%
echo Backend API: %BACKEND_URL%/api
echo Swagger UI: %BACKEND_URL%/swagger-ui.html ^(if enabled^)

echo.
echo 🏁 Next Steps:
echo 1. Test complete user flow in browser
echo 2. Create additional test data via frontend
echo 3. Verify real-time dashboard updates
echo 4. Test profit analytics page
echo 5. Test responsive design

echo.
pause
