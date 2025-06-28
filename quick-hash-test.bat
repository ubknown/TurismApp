@echo off
echo ========================================
echo QUICK PASSWORD HASH TEST
echo ========================================
echo.

echo Testing password hash with existing endpoint:
curl -s "http://localhost:8080/api/auth/test-password-hash?password=admin123"
echo.
echo.

echo ========================================
echo RESULTS INTERPRETATION:
echo ========================================
echo.
echo Look for these values in the JSON response:
echo.
echo ✅ SUCCESS PATTERN:
echo   "currentAdminHash": "$2a$10$JQnA..."
echo   "matchesCurrentHash": true
echo   "inputPassword": "admin123"
echo.
echo ❌ FAILURE PATTERN:
echo   "matchesCurrentHash": false
echo.
echo If "matchesCurrentHash" is false, the database hash is wrong!
echo.
pause
