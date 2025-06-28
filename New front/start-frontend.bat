@echo off
cls
echo ================================================
echo         STARTING TOURISM APP FRONTEND
echo ================================================
echo.

cd /d "D:\razu\Licenta\SCD\TurismApp\New front"

echo [1/4] Checking Node.js installation...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js not found! Please install Node.js 18+ from https://nodejs.org
    pause
    exit /b 1
)
echo ✅ Node.js is available
node --version
echo.

echo [2/4] Checking if frontend is already running...
netstat -an | findstr ":5173" >nul
if %errorlevel% == 0 (
    echo ⚠️ Frontend is already running on port 5173!
    echo   Close the existing instance first
    pause
    exit /b 1
)
echo ✅ Port 5173 is available
echo.

echo [3/4] Installing/checking dependencies...
if not exist "node_modules" (
    echo 📦 Installing npm dependencies...
    npm install
    if %errorlevel% neq 0 (
        echo ❌ Failed to install dependencies
        pause
        exit /b 1
    )
) else (
    echo ✅ Dependencies already installed
)
echo.

echo [4/4] Starting the development server...
echo.
echo 🎨 Frontend starting at: http://localhost:5173
echo 🔗 Backend API: http://localhost:8080
echo 📱 Mobile access: http://your-ip:5173
echo.
echo Press Ctrl+C to stop the frontend
echo.

npm run dev

pause
