@echo off
echo ==========================================
echo Testing Password Reset Functionality
echo ==========================================
echo.

set BACKEND_URL=http://localhost:8080
set TEST_EMAIL=test@example.com

echo Step 1: Testing /api/auth/forgot-password endpoint...
echo.
curl -X POST "%BACKEND_URL%/api/auth/forgot-password" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"%TEST_EMAIL%\"}" ^
  -w "\nStatus Code: %%{http_code}\n" ^
  -s

echo.
echo ==========================================
echo.

echo Step 2: Testing with invalid email format...
echo.
curl -X POST "%BACKEND_URL%/api/auth/forgot-password" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"invalid-email\"}" ^
  -w "\nStatus Code: %%{http_code}\n" ^
  -s

echo.
echo ==========================================
echo.

echo Step 3: Testing /api/auth/reset-password endpoint with dummy token...
echo.
curl -X POST "%BACKEND_URL%/api/auth/reset-password" ^
  -H "Content-Type: application/json" ^
  -d "{\"token\":\"dummy-token\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}" ^
  -w "\nStatus Code: %%{http_code}\n" ^
  -s

echo.
echo ==========================================
echo.

echo Step 4: Testing /api/auth/reset-password with mismatched passwords...
echo.
curl -X POST "%BACKEND_URL%/api/auth/reset-password" ^
  -H "Content-Type: application/json" ^
  -d "{\"token\":\"dummy-token\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"differentPassword123\"}" ^
  -w "\nStatus Code: %%{http_code}\n" ^
  -s

echo.
echo ==========================================
echo Testing complete!
echo.
echo Expected results:
echo - Step 1: Should return 200 with success message
echo - Step 2: Should return 400 with validation error  
echo - Step 3: Should return 400 with "Invalid token" error
echo - Step 4: Should return 400 with "Passwords don't match" error
echo.
echo If any endpoint returns 403 Forbidden, there's a Spring Security configuration issue.
echo If any endpoint returns 404 Not Found, check the controller mapping.
echo.
pause
