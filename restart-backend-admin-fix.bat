@echo off
echo ===============================================
echo RESTARTING BACKEND TO APPLY ADMIN LOGIN FIX
echo ===============================================
echo.

echo Step 1: Stopping any running backend processes...
taskkill /f /im java.exe > nul 2>&1
echo ✅ Stopped existing Java processes

echo.
echo Step 2: Waiting for graceful shutdown...
timeout /t 3 > nul

echo.
echo Step 3: Starting backend with admin login fix...
echo The SecurityConfig has been updated to allow /api/admin/login
echo.

cd /d "%~dp0"

echo Starting Spring Boot application...
call mvnw.cmd spring-boot:run

echo.
echo ===============================================
echo BACKEND RESTART COMPLETE
echo ===============================================
echo.
echo The admin login endpoint should now be accessible.
echo Test it with: test-admin-login-403-fix.bat
echo.
pause
