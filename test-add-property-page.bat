@echo off
echo ===============================================
echo TEST ADD PROPERTY PAGE SYSTEM
echo ===============================================
echo.

echo This script will test the enhanced Add Property page:
echo 1. Start the backend server
echo 2. Start the frontend server
echo 3. Guide you through testing the new form
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
echo Step 3: Starting frontend server...
echo ===================================
echo.
echo Starting frontend development server...
cd "New front"
start "Frontend Server" cmd /c "npm run dev"
cd ..

echo.
echo Waiting for frontend to start...
timeout /t 10 > nul

echo.
echo Step 4: Testing complete system...
echo ==================================
echo.
echo The application is now running. Please test the following manually:
echo.
echo 🔗 Frontend URL: http://localhost:5173
echo 🔧 Backend URL: http://localhost:8080
echo.
echo ✅ **Test Steps:**
echo.
echo 1. **Login as Owner:**
echo    - Go to http://localhost:5173/login
echo    - Use owner credentials (email: owner@test.com, password: test123)
echo.
echo 2. **Test Dashboard Navigation:**
echo    - After login, should redirect to Dashboard
echo    - Look for "Add New Property" button
echo    - Click the button - should navigate to /add-property
echo.
echo 3. **Test Add Property Form:**
echo    - ✅ Property Name field (required)
echo    - ✅ Property Type dropdown (required)
echo    - ✅ County dropdown (required)
echo    - ✅ Phone Number field (required)
echo    - ✅ Full Address field (required)
echo    - ✅ Guest Capacity field (required)
echo    - ✅ Price per Night field (required)
echo    - ✅ Description textarea (required, min 20 chars)
echo    - ✅ Amenities selection (optional)
echo    - ✅ Image upload gallery (required, 1-10 images)
echo.
echo 4. **Test Form Validation:**
echo    - Try submitting empty form - should show validation errors
echo    - Fill all required fields except images - should request images
echo    - Try uploading non-image files - should reject
echo    - Try uploading more than 10 images - should limit to 10
echo.
echo 5. **Test Form Submission:**
echo    - Fill all required fields correctly
echo    - Upload 1-3 test images
echo    - Submit form - should create property and redirect to My Units
echo.
echo 6. **Test Consistency:**
echo    - Go to "My Units" page
echo    - "Add New Property" button should also work
echo    - Empty state should show "Add Your First Property"
echo.
echo 🐛 **Common Issues to Check:**
echo.
echo ❌ If form doesn't load: Check frontend console for errors
echo ❌ If validation fails: Ensure all required fields are filled
echo ❌ If images don't upload: Check file types and sizes
echo ❌ If backend errors: Check backend logs for details
echo ❌ If navigation fails: Verify routing configuration
echo.
echo 📱 **UI Design Check:**
echo.
echo ✅ Consistent glassmorphism design with rest of site
echo ✅ Proper form sections with icons and headers
echo ✅ Responsive layout on different screen sizes
echo ✅ Loading states and error messages
echo ✅ Image gallery with preview and remove functionality
echo.
echo ===============================================
echo MANUAL TESTING REQUIRED
echo ===============================================
echo.
echo The servers are now running. Please perform the manual tests above.
echo When done testing, you can close both server windows.
echo.
pause
