@echo off
echo ========================================
echo TESTING CORS CONFIGURATION FIX
echo ========================================
echo.

echo This script tests if the CORS error is fixed and frontend can access backend.
echo.

echo Step 1: Check if backend is running...
netstat -an | findstr :8080 | findstr LISTENING
if %errorlevel% neq 0 (
    echo ERROR: Backend not running on port 8080
    echo Please start the backend first: mvnw.cmd spring-boot:run
    pause
    exit /b
)
echo ✅ Backend is running on port 8080
echo.

echo Step 2: Testing CORS preflight request (OPTIONS)...
echo.
echo Testing OPTIONS request to auth endpoint:
curl -X OPTIONS http://localhost:8080/api/auth/login ^
  -H "Origin: http://localhost:5173" ^
  -H "Access-Control-Request-Method: POST" ^
  -H "Access-Control-Request-Headers: Content-Type" ^
  -v
echo.
echo.

echo Step 3: Testing actual request with credentials...
echo.
echo Testing POST request with CORS headers:
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Origin: http://localhost:5173" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@test.com\",\"password\":\"wrong\"}" ^
  -v
echo.
echo.

echo Step 4: Testing forgot-password endpoint with CORS...
echo.
echo Testing forgot-password with CORS headers:
curl -X POST http://localhost:8080/api/auth/forgot-password ^
  -H "Origin: http://localhost:5173" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\"}" ^
  -v
echo.
echo.

echo ========================================
echo INTERPRETATION OF RESULTS:
echo ========================================
echo.
echo Look for these CORS headers in the responses above:
echo ✅ Access-Control-Allow-Origin: http://localhost:5173
echo ✅ Access-Control-Allow-Credentials: true
echo ✅ Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
echo ✅ Access-Control-Allow-Headers: Authorization, Content-Type, Accept...
echo.
echo ❌ If you see "CORS policy" errors, the fix needs adjustment
echo ❌ If Access-Control-Allow-Origin is "*", there's still a wildcard issue
echo.
echo Common success indicators:
echo - OPTIONS requests return 200 with proper CORS headers
echo - POST requests return 400/401 (business logic) but NOT CORS errors
echo - No "blocked by CORS policy" messages
echo.
pause
