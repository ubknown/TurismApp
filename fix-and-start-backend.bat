@echo off
echo � Tourism App Backend - Clean Startup
echo ====================================
echo.

cd /d "c:\Users\razvi\Desktop\SCD\TurismApp"

echo � Pre-startup checks...
echo.

REM Check if backend is already running
echo Checking port 8080...
netstat -an | findstr ":8080" >nul
if %errorlevel% == 0 (
    echo ⚠️  Port 8080 is already in use!
    echo.
    echo Attempting to stop existing process...
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080"') do (
        echo Killing process %%a
        taskkill /F /PID %%a >nul 2>&1
    )
    timeout /t 3 /nobreak >nul
)

REM Check Java
echo Checking Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found! Please install Java 17+
    pause
    exit /b 1
)

REM Check Maven
echo Checking Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven not found! Please install Maven
    pause
    exit /b 1
)

echo ✅ All checks passed!
echo.

echo 🧹 Cleaning previous build...
mvn clean >nul 2>&1

echo 🚀 Starting Spring Boot backend...
echo.
echo 📊 Expected output:
echo   - "Started TurismappApplication in X.XXX seconds"
echo   - "Tomcat started on port(s): 8080"
echo.
echo 🌐 Once started, access:
echo   - Health: http://localhost:8080/actuator/health
echo   - API: http://localhost:8080/api/units/public
echo.
echo Press Ctrl+C to stop the server
echo.

REM Start with verbose output
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m"
