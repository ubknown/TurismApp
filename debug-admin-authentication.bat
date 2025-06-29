@echo off
echo ========================================
echo COMPREHENSIVE AUTHENTICATION DEBUG
echo ========================================
echo.

echo Testing authentication for admin@tourism.com with password admin123
echo This will help identify exactly where the authentication is failing.
echo.

echo Step 1: Basic user lookup...
echo ----------------------------------------
curl -X GET "http://localhost:8080/api/auth/debug/user?email=admin@tourism.com" ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo Step 2: Password hash verification...
echo ----------------------------------------
curl -X POST "http://localhost:8080/api/auth/debug/verify-password" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"rawPassword\":\"admin123\"}" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo Step 3: UserDetailsService test...
echo ----------------------------------------
curl -X POST "http://localhost:8080/api/auth/debug/test-userdetails" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\"}" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo Step 4: Full authentication test...
echo ----------------------------------------
curl -X POST "http://localhost:8080/api/auth/debug/test-authentication" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo Step 5: Actual login attempt...
echo ----------------------------------------
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo ========================================
echo DEBUG ANALYSIS COMPLETE
echo ========================================
echo.
echo If Step 1 fails: User doesn't exist or database issue
echo If Step 2 fails: Password hash doesn't match
echo If Step 3 fails: UserDetailsService configuration issue  
echo If Step 4 fails: AuthenticationManager configuration issue
echo If Step 5 fails: Check the specific step that failed above
echo.
echo Check backend logs for detailed error information.
echo.
pause
