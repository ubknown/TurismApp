@echo off
echo ================================================================
echo PLAIN TEXT PASSWORD CONFIGURATION VERIFICATION
echo ================================================================
echo.

echo STEP 1: Check SecurityConfig.java
echo ---------------------------------
findstr /n "NoOpPasswordEncoder" "src\main\java\com\licentarazu\turismapp\config\SecurityConfig.java" 2>nul
if %errorlevel% equ 0 (
    echo ✅ SecurityConfig.java configured for plain text passwords
) else (
    echo ❌ SecurityConfig.java NOT configured - check NoOpPasswordEncoder
)
echo.

echo STEP 2: Database Setup
echo ----------------------
echo Run this SQL to create admin user:
echo    mysql -u root -p turismdb ^< create-admin-plaintext.sql
echo.
echo Verify with:
echo    USE turismdb;
echo    SELECT email, password, role FROM users WHERE email = 'admin@turismapp.com';
echo Expected: password should show 'admin123' (plain text, not hash)
echo.

echo STEP 3: Test Authentication
echo ---------------------------
echo After starting backend, run:
echo    test-admin-plaintext.bat
echo.
echo Both endpoints should return HTTP 200:
echo    - /api/admin/login
echo    - /api/auth/login
echo.

echo STEP 4: Admin Dashboard Test
echo ----------------------------
echo 1. Navigate to: http://localhost:5173/admin
echo 2. Login with: admin@turismapp.com / admin123
echo 3. Verify:
echo    ✅ Admin login required (even if logged in as other user)
echo    ✅ All owner applications displayed
echo    ✅ Accept/Refuse buttons work
echo    ✅ Database updates after actions
echo    ✅ UI refreshes after admin actions
echo.

echo ================================================================
echo SECURITY WARNING: Plain text passwords are NOT secure!
echo This configuration is for development/testing ONLY.
echo ================================================================
echo.
pause
