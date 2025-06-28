@echo off
echo ========================================
echo ISOLATED PASSWORD MATCHING DEBUG
echo ========================================
echo.
echo This will test ONLY password matching, isolated from Spring Security
echo.

REM Check if backend is running
curl -s "http://localhost:8080/api/auth/test-email-config" > nul
if errorlevel 1 (
    echo ❌ ERROR: Backend is not running on port 8080
    echo Please start the backend first with: mvnw spring-boot:run
    pause
    exit /b 1
) else (
    echo ✅ Backend is running
)

echo.
echo ========================================
echo TESTING PASSWORD MATCHING ISOLATED
echo ========================================
echo.

echo Testing password matching for admin@tourism.com with password admin123:
curl -X POST "http://localhost:8080/api/debug/test-password-match" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}" ^
  -w "\nHTTP Status: %%{http_code}\n"

echo.
echo.
echo ========================================
echo WHAT TO LOOK FOR:
echo ========================================
echo.
echo ✅ SUCCESS INDICATORS:
echo   - "userFound": true
echo   - "userEnabled": true
echo   - "passwordMatches": true
echo   - "knownGoodHashMatches": true
echo.
echo ❌ FAILURE INDICATORS:
echo   - "userFound": false = User doesn't exist in database
echo   - "passwordMatches": false = Password hash doesn't match
echo   - "userEnabled": false = Account is disabled
echo.
echo ========================================
echo NEXT STEPS BASED ON RESULTS:
echo ========================================
echo.
echo IF passwordMatches = true:
echo   - Password hash is correct
echo   - Problem is in Spring Security authentication flow
echo   - Check UserDetailsServiceImpl and Security config
echo.
echo IF passwordMatches = false:
echo   - Password hash in database is wrong
echo   - Update database with correct hash
echo   - Run: mysql -u root -p
echo   - UPDATE users SET password = '[new_hash]' WHERE email = 'admin@tourism.com';
echo.
echo IF userFound = false:
echo   - Admin user doesn't exist in database
echo   - Create admin user with correct data
echo.

echo.
echo ========================================
echo BACKEND LOGS TO CHECK:
echo ========================================
echo.
echo Look for these messages in your backend console:
echo   🔍 ===== DEBUG PASSWORD MATCHING =====
echo   🔍 Email: 'admin@tourism.com'
echo   🔍 Password: 'admin123'
echo   ✅ User found: ID=X, Email='admin@tourism.com', Enabled=true
echo   🔑 DB Password Hash: '$2a$10$...'
echo   🧪 About to call encoder.matches(...)
echo   🔍 encoder.matches() result: true/false
echo.

pause
