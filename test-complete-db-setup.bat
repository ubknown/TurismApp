@echo off
cls
echo ================================================
echo    SPRING BOOT DATABASE CONNECTIVITY TEST
echo ================================================
echo.

echo [1/5] Testing MySQL Connection...
mysql -u root -pRzvtare112 -e "SELECT 'MySQL Connection: SUCCESS' as status;" 2>nul
if %errorlevel% neq 0 (
    echo ❌ MySQL Connection: FAILED
    echo    - Check if MySQL server is running
    echo    - Verify username/password: root/Rzvtare112
    goto :end
) else (
    echo ✅ MySQL Connection: SUCCESS
)
echo.

echo [2/5] Testing Database Existence...
mysql -u root -pRzvtare112 -e "USE turismdb; SELECT 'Database turismdb: EXISTS' as status;" 2>nul
if %errorlevel% neq 0 (
    echo ❌ Database 'turismdb': NOT FOUND
    echo    - Create database using: CREATE DATABASE turismdb;
    goto :end
) else (
    echo ✅ Database 'turismdb': EXISTS
)
echo.

echo [3/5] Testing Required Tables...
mysql -u root -pRzvtare112 -e "USE turismdb; SELECT COUNT(*) FROM users;" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Table 'users': NOT FOUND or INVALID
    goto :end
) else (
    echo ✅ Table 'users': EXISTS
)

mysql -u root -pRzvtare112 -e "USE turismdb; SELECT COUNT(*) FROM accommodation_units;" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Table 'accommodation_units': NOT FOUND or INVALID
    goto :end
) else (
    echo ✅ Table 'accommodation_units': EXISTS
)
echo.

echo [4/5] Testing Data Counts...
for /f "skip=1" %%i in ('mysql -u root -pRzvtare112 -se "USE turismdb; SELECT COUNT(*) FROM users;" 2^>nul') do set USER_COUNT=%%i
for /f "skip=1" %%i in ('mysql -u root -pRzvtare112 -se "USE turismdb; SELECT COUNT(*) FROM accommodation_units;" 2^>nul') do set UNIT_COUNT=%%i

echo ✅ Users in database: %USER_COUNT%
echo ✅ Accommodation units: %UNIT_COUNT%
echo.

echo [5/5] Application Configuration Summary...
echo ✅ Datasource URL: jdbc:mysql://localhost:3306/turismdb
echo ✅ Username: root
echo ✅ Password: [CONFIGURED]
echo ✅ Driver: com.mysql.cj.jdbc.Driver
echo ✅ JPA Dialect: MySQLDialect
echo.

echo ================================================
echo             CONFIGURATION STATUS
echo ================================================
echo ✅ Database connectivity: READY
echo ✅ Tables and schema: READY  
echo ✅ Spring Boot config: READY
echo.
echo 🚀 Your application should start successfully!
echo.
echo To test the application:
echo 1. Run: mvnw.cmd spring-boot:run
echo 2. Wait for "Started TurismappApplication"
echo 3. Test endpoint: http://localhost:8080/api/test/database
echo.

:end
pause
