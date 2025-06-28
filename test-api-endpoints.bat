@echo off
echo Testing Spring Boot Application Database Endpoints...
echo.

echo [1] Testing database connectivity endpoint...
curl -X GET "http://localhost:8080/api/test/database" -H "Content-Type: application/json" 2>nul
if %errorlevel% neq 0 (
    echo ❌ Application not running or endpoint failed
    echo    Make sure the Spring Boot application is started first
    goto :end
)
echo.
echo.

echo [2] Testing user registration endpoint...
curl -X POST "http://localhost:8080/api/auth/register" ^
     -H "Content-Type: application/json" ^
     -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"password\":\"password123\",\"role\":\"GUEST\"}" 2>nul
echo.
echo.

echo [3] Testing accommodation units endpoint...
curl -X GET "http://localhost:8080/api/units" -H "Content-Type: application/json" 2>nul
echo.
echo.

:end
echo.
echo Test completed!
pause
