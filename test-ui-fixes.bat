@echo off
echo ========================================
echo 🧪 TESTING BOOKING AND FILTER FIXES
echo ========================================
echo.

set "BACKEND_URL=http://localhost:8080"

echo Step 1: Health Check
curl -s "%BACKEND_URL%/api/units/debug/health"
echo.
echo.

echo Step 2: Test My Bookings Endpoint (requires authentication)
echo NOTE: This will return 403 without proper JWT token
echo Testing structure only...
curl -v -s "%BACKEND_URL%/api/bookings/my-bookings" 2>&1 | find "HTTP"
echo.

echo ========================================
echo MANUAL TESTING STEPS:
echo ========================================
echo.
echo 1. FILTER BUTTON TEST:
echo    - Start your React frontend
echo    - Go to Browse Units page
echo    - Notice "Apply Filters" button is initially grayed out
echo    - Set any filter (dates, location, price, etc.)
echo    - Button should become bright blue and clickable
echo    - Click to apply filters
echo.
echo 2. BOOKING DETAILS TEST:
echo    - Login to your app
echo    - Go to "My Bookings" page
echo    - Bookings should show real unit names and locations
echo    - No more "Unknown Unit" or "Unknown Location"
echo.
echo 3. BACKEND RESTART REQUIRED:
echo    - Stop your Spring Boot backend (Ctrl+C)
echo    - Run: mvn spring-boot:run
echo    - Wait for startup completion
echo    - Test the frontend changes
echo.
echo ========================================
pause
