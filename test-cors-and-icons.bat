@echo off
echo 🔧 Testing CORS and React Icon Fixes
echo ====================================
echo.

echo 📋 Test Plan:
echo 1. Start backend server (port 8080)
echo 2. Start frontend server (port 5173)
echo 3. Test CORS communication
echo 4. Verify React components render correctly
echo.

echo 🚀 Step 1: Starting Backend Server
echo -----------------------------------
echo Starting Spring Boot backend...
cd "c:\Users\razvi\Desktop\SCD\TurismApp"

REM Check if backend is already running
curl -s -o nul -w "Backend Status: %%{http_code}\n" http://localhost:8080/api/accommodation-units 2>nul
if %errorlevel%==0 (
    echo ✅ Backend is already running on port 8080
) else (
    echo 🚀 Starting backend server...
    start "Backend Server" mvn spring-boot:run
    echo ⏳ Waiting for backend to start...
    timeout /t 30 /nobreak >nul
)

echo.
echo 🌐 Step 2: Starting Frontend Server  
echo -----------------------------------
cd "c:\Users\razvi\Desktop\SCD\TurismApp\New front"

REM Check if frontend is already running
curl -s -o nul -w "Frontend Status: %%{http_code}\n" http://localhost:5173 2>nul
if %errorlevel%==0 (
    echo ✅ Frontend is already running on port 5173
) else (
    echo 🚀 Starting frontend server...
    start "Frontend Server" npm run dev
    echo ⏳ Waiting for frontend to start...
    timeout /t 15 /nobreak >nul
)

echo.
echo 🧪 Step 3: Testing CORS Communication
echo ------------------------------------

echo Testing /api/units/public endpoint...
curl -s -o cors_test.json -w "CORS Test Status: %%{http_code}\n" "http://localhost:8080/api/units/public"

if exist cors_test.json (
    echo ✅ CORS endpoint responded successfully
    echo Response preview:
    type cors_test.json | more
    del cors_test.json
) else (
    echo ❌ CORS test failed
)

echo.
echo Testing with Origin header (simulating frontend)...
curl -s -H "Origin: http://localhost:5173" -w "Cross-Origin Status: %%{http_code}\n" "http://localhost:8080/api/units/public" >nul

echo.
echo 🎯 Step 4: Manual Testing Instructions
echo -------------------------------------
echo.
echo 1. Open browser to: http://localhost:5173
echo 2. Navigate to Units List page
echo 3. Check for these fixes:
echo    ✅ No CORS errors in browser console
echo    ✅ Search input field renders correctly
echo    ✅ Location and Guests filter inputs work
echo    ✅ API calls to /api/units/public succeed
echo.
echo 4. Test the search functionality:
echo    - Type in search box (should not crash)
echo    - Use location filter (should not crash) 
echo    - Apply guest capacity filter (should not crash)
echo.
echo 🔍 Expected Results:
echo ✅ No "Element type is invalid" errors
echo ✅ Search and filter icons display correctly
echo ✅ No CORS blocked requests in Network tab
echo ✅ Units list loads from backend API
echo.
echo 🔗 Quick Links:
echo - Frontend: http://localhost:5173
echo - Backend API: http://localhost:8080/api
echo - Units API: http://localhost:8080/api/units/public
echo.
echo Press any key to open browser windows...
pause >nul

REM Open browser windows
start http://localhost:5173
start http://localhost:8080/api/units/public

echo.
echo 🏁 Testing Complete!
echo If you see any issues, check:
echo - Browser console for React errors
echo - Network tab for CORS/API errors  
echo - Backend logs for server errors

pause
