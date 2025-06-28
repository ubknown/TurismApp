@echo off
echo 🚀 Starting Tourism App Frontend...
echo ================================

cd "c:\Users\razvi\Desktop\SCD\TurismApp\New front"

echo 📦 Installing dependencies...
call npm install

echo 🎯 Starting development server...
echo Frontend will be available at: http://localhost:5173
echo Backend should be running at: http://localhost:8080
echo.
echo Press Ctrl+C to stop the development server
echo.

call npm run dev

pause
