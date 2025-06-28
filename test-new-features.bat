@echo off
echo Testing Tourism App Implementation...
echo.

echo 1. Starting Backend (Spring Boot)...
start "TurismApp Backend" cmd /k "cd /d D:\razu\Licenta\SCD\TurismApp && mvnw.cmd spring-boot:run"

echo.
echo Waiting 15 seconds for backend to start...
timeout /t 15 /nobreak > nul

echo.
echo 2. Starting Frontend (React Vite)...
start "TurismApp Frontend" cmd /k "cd /d D:\razu\Licenta\SCD\TurismApp\New front && npm run dev"

echo.
echo 3. Opening browser to test the application...
timeout /t 5 /nobreak > nul
start "" "http://localhost:5173"

echo.
echo Testing Features:
echo - Registration with email confirmation
echo - Location filtering by county
echo - Property creation with county + address
echo.
echo Check the following:
echo ✓ Register a new user and verify the confirmation email notification
echo ✓ Filter accommodations by county in the units list
echo ✓ Create a new property as an OWNER with county selection and exact address
echo.
pause
