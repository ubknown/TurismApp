@echo off
echo ========================================
echo      TurismApp - Diploma Project
echo         Quick Startup Script
echo ========================================
echo.

echo [1] Starting Backend (Spring Boot)...
echo Please wait for "Started TurismappApplication" message
echo Backend will run on: http://localhost:8080
echo.

start cmd /k "title Backend - TurismApp && mvnw.cmd spring-boot:run"

echo [2] Waiting 10 seconds for backend to initialize...
timeout /t 10 /nobreak

echo [3] Starting Frontend (React + Vite)...
echo Frontend will run on: http://localhost:5173
echo.

cd "New front"
start cmd /k "title Frontend - TurismApp && npm run dev"

echo.
echo ========================================
echo    Both services are starting up!
echo ========================================
echo.
echo Backend Health Check: http://localhost:8080/api/units/debug/health
echo Frontend Application: http://localhost:5173
echo.
echo Press any key to open browser...
pause

start http://localhost:5173

echo.
echo Startup complete! Good luck with your diploma presentation!
pause
