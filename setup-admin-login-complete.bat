@echo off
echo ===============================================
echo COMPLETE ADMIN LOGIN SETUP AND FIX
echo ===============================================
echo.

echo This script will:
echo 1. Create admin user in database
echo 2. Restart backend with 403 fix
echo 3. Test admin login functionality
echo.

set /p continue="Continue with setup? (y/n): "
if /i not "%continue%"=="y" (
    echo Setup cancelled.
    pause
    exit /b 0
)

echo.
echo Step 1: Creating admin user in database...
echo ========================================
echo.
echo Please run the following SQL script in your MySQL database:
echo.
type create-admin-plaintext.sql
echo.
echo Copy the above SQL and run it in your MySQL client.
echo.
set /p db_done="Have you run the SQL script? (y/n): "
if /i not "%db_done%"=="y" (
    echo Please run the SQL script first, then restart this setup.
    pause
    exit /b 0
)

echo.
echo Step 2: Restarting backend with admin login fix...
echo ==============================================
echo.
echo Stopping any running backend processes...
taskkill /f /im java.exe > nul 2>&1
echo ✅ Stopped existing processes

echo.
echo Starting backend with updated SecurityConfig...
echo (This will start the backend in a new window)
echo.
start "Backend Server" cmd /c "mvnw.cmd spring-boot:run"

echo.
echo Waiting for backend to start...
timeout /t 10 > nul

echo.
echo Step 3: Testing admin login...
echo =============================
echo.

:test_loop
echo Testing backend health...
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% neq 0 (
    echo Backend not ready yet, waiting...
    timeout /t 5 > nul
    goto test_loop
)

echo ✅ Backend is running
echo.
echo Testing admin login endpoint...
echo.

curl -X POST http://localhost:8080/api/admin/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@turismapp.com\",\"password\":\"admin123\"}" ^
  -w "HTTP Status: %%{http_code}\n"

echo.
echo.
echo ===============================================
echo SETUP COMPLETE
echo ===============================================
echo.
echo If you see HTTP Status: 200 above, the fix is working!
echo If you still get 403 or other errors, check the troubleshooting guide:
echo ADMIN_LOGIN_403_FIX_GUIDE.md
echo.
echo Admin Credentials:
echo Email: admin@turismapp.com
echo Password: admin123
echo.
echo You can now access the admin dashboard in your frontend application.
echo.
pause
