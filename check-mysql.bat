@echo off
echo 🔍 Checking MySQL Database Connection...
echo.

REM Test if MySQL is running
echo Testing MySQL service...
sc query mysql >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ MySQL service is installed
) else (
    echo ⚠️  MySQL service not found - checking if running on port 3306...
)

echo.
echo Testing MySQL port 3306...
netstat -an | findstr ":3306" >nul
if %errorlevel% == 0 (
    echo ✅ MySQL is listening on port 3306
) else (
    echo ❌ MySQL is not running on port 3306!
    echo.
    echo 💡 Solutions:
    echo    1. Start MySQL service: net start mysql
    echo    2. Start XAMPP/WAMP if using those
    echo    3. Check MySQL installation
    echo.
    pause
    exit /b 1
)

echo.
echo 📊 Database Configuration (from application.properties):
echo    URL: jdbc:mysql://localhost:3306/turismdb
echo    Username: root
echo    Database: turismdb
echo.

echo ✅ MySQL appears to be running!
echo   Backend should be able to connect to database.
echo.
pause
