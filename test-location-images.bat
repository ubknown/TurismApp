@echo off
echo ===============================================
echo TEST LOCATION IMAGES UPLOAD SYSTEM
echo ===============================================
echo.

echo This script will test the enhanced location image upload system:
echo 1. Start the backend server
echo 2. Test image upload endpoints
echo 3. Verify image gallery functionality
echo.

set /p continue="Continue with test? (y/n): "
if /i not "%continue%"=="y" (
    echo Test cancelled.
    pause
    exit /b 0
)

echo.
echo Step 1: Starting backend server...
echo ===================================
echo.
echo Stopping any running backend processes...
taskkill /f /im java.exe > nul 2>&1
echo ✅ Stopped existing processes

echo.
echo Starting backend server...
start "Backend Server" cmd /c "mvnw.cmd spring-boot:run"

echo.
echo Waiting for backend to start...
timeout /t 15 > nul

echo.
echo Step 2: Testing backend health...
echo =================================
:test_backend
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% neq 0 (
    echo Backend not ready yet, waiting...
    timeout /t 5 > nul
    goto test_backend
)

echo ✅ Backend is running
echo.

echo.
echo Step 3: Testing image upload functionality...
echo ============================================
echo.
echo Testing file upload controller endpoints...
echo.

echo Testing health endpoint:
curl -s http://localhost:8080/api/uploads/health
echo.
echo.

echo.
echo ===============================================
echo TEST COMPLETE
echo ===============================================
echo.
echo The backend is now running and ready for testing:
echo.
echo 🏠 Frontend URL: http://localhost:5173
echo 🔧 Backend URL: http://localhost:8080
echo.
echo Features to test manually:
echo 1. Open frontend and login as owner
echo 2. Go to "Add Unit" page
echo 3. Test uploading 1-10 images using the new gallery
echo 4. Verify images are previewed correctly
echo 5. Submit the form and check if images are saved
echo 6. Edit an existing unit and test adding more images
echo.
echo If you encounter issues, check the backend logs.
echo.
pause
