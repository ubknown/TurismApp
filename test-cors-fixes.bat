@echo off
echo ========================================
echo CORS FIXES VERIFICATION SCRIPT
echo ========================================
echo.
echo This script will test the CORS configuration fixes:
echo 1. Fixed @CrossOrigin annotations to remove wildcards
echo 2. Added withCredentials: true to frontend axios
echo 3. Verified SecurityConfig CORS settings
echo.

echo Checking if backend is running on port 8080...
netstat -an | findstr :8080 > nul
if errorlevel 1 (
    echo ERROR: Backend is not running on port 8080
    echo Please start the backend first with: mvn spring-boot:run
    echo Or use: start-backend.bat
    pause
    exit /b 1
) else (
    echo ✓ Backend is running on port 8080
)

echo.
echo Testing CORS with specific origins (no wildcards)...
echo.

echo 1. Testing OPTIONS preflight request to /api/auth/login:
curl -X OPTIONS "http://localhost:8080/api/auth/login" ^
  -H "Origin: http://localhost:5173" ^
  -H "Access-Control-Request-Method: POST" ^
  -H "Access-Control-Request-Headers: Content-Type,Authorization" ^
  -i

echo.
echo.

echo 2. Testing GET request to public auth endpoint:
curl -X GET "http://localhost:8080/api/auth/test-email-config" ^
  -H "Origin: http://localhost:5173" ^
  -H "Content-Type: application/json" ^
  -i

echo.
echo.

echo 3. Testing POST to auth endpoint with credentials:
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Origin: http://localhost:5173" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"nonexistent@test.com\",\"password\":\"testpass\"}" ^
  -i

echo.
echo.

echo ========================================
echo CORS FIXES APPLIED:
echo ========================================
echo 1. ✓ Fixed AuthController @CrossOrigin - removed wildcard "*"
echo 2. ✓ Fixed ReservationController @CrossOrigin - removed wildcard "*"  
echo 3. ✓ Fixed ReviewController @CrossOrigin - removed wildcard "*"
echo 4. ✓ Fixed UserController @CrossOrigin - removed wildcard "*"
echo 5. ✓ Fixed FileUploadController @CrossOrigin - removed wildcard "*"
echo 6. ✓ Added withCredentials: true to frontend axios config
echo 7. ✓ SecurityConfig CORS properly configured with specific origins
echo.
echo EXPECTED RESULTS:
echo - Should see "Access-Control-Allow-Origin: http://localhost:5173" in responses
echo - Should see "Access-Control-Allow-Credentials: true" in responses
echo - No more CORS errors when accessing from http://localhost:5173
echo.
echo If you still see CORS errors:
echo 1. Make sure frontend is running on http://localhost:5173
echo 2. Clear browser cache and reload
echo 3. Check browser dev tools for specific error messages
echo 4. Verify both frontend and backend are running
echo.
pause
