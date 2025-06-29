@echo off
echo ===============================================
echo BACKEND RESTART FOR REGISTRATION FLOW
echo ===============================================
echo.

echo Checking backend status...
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Backend is not running
    echo.
    echo Step 1: Stopping any existing Java processes...
    taskkill /f /im java.exe > nul 2>&1
    echo ✅ Cleared any existing processes
    echo.
    
    echo Step 2: Starting backend server...
    echo (This will open in a new window)
    start "TurismApp Backend" cmd /c "cd /d d:\razu\Licenta\SCD\TurismApp && mvnw.cmd spring-boot:run"
    echo.
    
    echo Step 3: Waiting for backend to start...
    echo Please wait 30-45 seconds for the backend to fully start...
    
    :wait_loop
    timeout /t 5 > nul
    echo Checking if backend is ready...
    curl -s http://localhost:8080/actuator/health > nul 2>&1
    if %errorlevel% neq 0 (
        echo Still starting... please wait
        goto wait_loop
    )
    
    echo ✅ Backend is now running!
) else (
    echo ✅ Backend is already running
)

echo.
echo Step 4: Testing registration endpoint...
echo ======================================
echo.

echo Testing user registration:
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"password\":\"TestPass123\",\"role\":\"GUEST\"}" ^
  -w "\nHTTP Status: %%{http_code}\n"

echo.
echo.
echo REGISTRATION FLOW TEST RESULTS:
echo ==============================
echo.
echo Expected:
echo - HTTP Status: 201 (if new user)
echo - HTTP Status: 200 (if user exists but not confirmed)
echo - HTTP Status: 409 (if user already confirmed)
echo.
echo If you see HTTP Status 201 or 200, the registration flow is working!
echo.
echo Next steps:
echo 1. Start frontend: cd "New front" && npm run dev
echo 2. Test registration at: http://localhost:5173/register
echo 3. Verify 3-second verification message display
echo 4. Check automatic redirect to login page
echo.

pause
