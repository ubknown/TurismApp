@echo off
cls
echo ================================================
echo      TOURISM APP - FULL STACK LAUNCHER
echo ================================================
echo.

echo This script will start both backend and frontend services.
echo.
echo Services to start:
echo 🔥 Backend  (Spring Boot) - http://localhost:8080
echo 🎨 Frontend (React/Vite)  - http://localhost:5173
echo.

set /p choice="Do you want to start both services? (y/n): "
if /i "%choice%" neq "y" (
    echo Cancelled by user
    pause
    exit /b 0
)

echo.
echo ================================================
echo              STARTING BACKEND
echo ================================================

REM Start backend in a new window
start "Tourism App - Backend" /d "D:\razu\Licenta\SCD\TurismApp" start-backend.bat

echo Waiting 10 seconds for backend to initialize...
timeout /t 10 /nobreak >nul

echo.
echo ================================================
echo              STARTING FRONTEND
echo ================================================

REM Start frontend in a new window
start "Tourism App - Frontend" /d "D:\razu\Licenta\SCD\TurismApp\New front" start-frontend.bat

echo.
echo ================================================
echo              SERVICES STARTED
echo ================================================
echo.
echo 🔥 Backend:  http://localhost:8080
echo 🎨 Frontend: http://localhost:5173
echo.
echo 📋 Quick Health Check:
echo.

REM Wait a moment and then test if services are running
timeout /t 5 /nobreak >nul

echo Testing backend health...
curl -s http://localhost:8080/api/test/database >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Backend is responding
) else (
    echo ⚠️ Backend not responding yet (may still be starting...)
)

echo.
echo Testing frontend...
curl -s http://localhost:5173 >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Frontend is responding
) else (
    echo ⚠️ Frontend not responding yet (may still be starting...)
)

echo.
echo ================================================
echo                NEXT STEPS
echo ================================================
echo.
echo 1. Open your browser and go to: http://localhost:5173
echo 2. Test user registration with your email
echo 3. Check backend logs for email confirmation URLs
echo.
echo To stop services:
echo - Close the backend and frontend terminal windows
echo - Or press Ctrl+C in each window
echo.

pause
