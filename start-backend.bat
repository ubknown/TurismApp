@echo off
echo 🚀 Starting Tourism App Backend Server
echo ==========================================
echo.

cd /d "D:\razu\Licenta\SCD\TurismApp"

echo 📋 Pre-Startup Checks:
echo.

REM Check if port 8080 is available
echo 🔍 Checking port 8080 availability...
netstat -an | findstr :8080 >nul
if %errorlevel%==0 (
    echo ⚠️  WARNING: Port 8080 is already in use!
    echo   You may need to stop other services or change the port
    echo   To change port, add this to application.properties:
    echo   server.port=8081
    echo.
) else (
    echo ✅ Port 8080 is available
)

REM Check Java version
echo 🔍 Checking Java version...
java -version 2>&1 | findstr "version" >nul
if %errorlevel%==0 (
    echo ✅ Java is installed
    java -version 2>&1 | findstr "version"
) else (
    echo ❌ Java not found in PATH
    echo   Please ensure Java 17+ is installed and in PATH
)

REM Check Maven
echo 🔍 Checking Maven...
mvn -version >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Maven is available
) else (
    echo ⚠️  Maven not found, will try Maven wrapper
)

echo.
echo 📂 Project Configuration:
echo - Database: MySQL on localhost:3306/turismdb
echo - Username: root
echo - Port: 8080 (default)
echo - Profile: default
echo.

echo 🚀 Starting Spring Boot application...
echo ==========================================
echo.
echo Looking for these startup messages:
echo ✅ "Started TurismappApplication in X.XXX seconds"
echo ✅ "Tomcat started on port(s): 8080"
echo ✅ "Database connection successful"
echo.
echo If you see errors:
echo - MySQL connection: Check if MySQL is running
echo - Port conflict: Change port in application.properties  
echo - Bean creation: Check for missing dependencies
echo.
echo Press Ctrl+C to stop the server when needed
echo.

REM Try Maven wrapper first, then regular Maven
if exist mvnw.cmd (
    echo Using Maven wrapper...
    mvnw.cmd spring-boot:run
) else (
    echo Using system Maven...
    mvn spring-boot:run
)

echo.
echo 🏁 Server stopped
pause
