@echo off
echo ==========================================
echo BOOKING CANCELLATION 403 ERROR DEBUG
echo ==========================================
echo.

REM Test if backend is running
echo Step 1: Test backend health...
curl -s http://localhost:8080/actuator/health
if %ERRORLEVEL% neq 0 (
    echo ❌ Backend is not running! Start it first with: mvn spring-boot:run
    pause
    exit /b 1
)
echo ✅ Backend is running
echo.

REM Get a valid JWT token (you'll need to update these credentials)
echo Step 2: Getting JWT token...
echo Enter your email:
set /p USER_EMAIL=
echo Enter your password:
set /p USER_PASSWORD=

echo Testing login...
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"%USER_EMAIL%\",\"password\":\"%USER_PASSWORD%\"}" ^
  -o login_response.json

if %ERRORLEVEL% neq 0 (
    echo ❌ Login failed
    pause
    exit /b 1
)

REM Extract token from response (basic parsing)
echo.
echo Login response:
type login_response.json
echo.

echo Enter the JWT token from the response above:
set /p JWT_TOKEN=

REM Test token validation
echo.
echo Step 3: Testing JWT token validation...
curl -X GET http://localhost:8080/api/auth/me ^
  -H "Authorization: Bearer %JWT_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -w "HTTP Status: %%{http_code}\n"

echo.
echo Step 4: Testing booking cancellation with different booking IDs...
echo Enter a booking ID to test:
set /p BOOKING_ID=

echo Testing cancel booking endpoint...
curl -X PUT http://localhost:8080/api/bookings/%BOOKING_ID%/cancel ^
  -H "Authorization: Bearer %JWT_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -w "HTTP Status: %%{http_code}\n" ^
  -v

echo.
echo Step 5: Testing with OPTIONS preflight (CORS)...
curl -X OPTIONS http://localhost:8080/api/bookings/%BOOKING_ID%/cancel ^
  -H "Origin: http://localhost:5173" ^
  -H "Access-Control-Request-Method: PUT" ^
  -H "Access-Control-Request-Headers: Authorization,Content-Type" ^
  -w "HTTP Status: %%{http_code}\n" ^
  -v

echo.
echo Debug complete! Check the responses above.
echo.
pause
