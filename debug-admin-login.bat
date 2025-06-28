@echo off
echo ========================================
echo ADMIN LOGIN DEBUG SCRIPT
echo ========================================
echo.
echo This script will help debug the admin login issue.
echo.

echo Step 1: Testing database connection and checking if admin user exists...
echo.

echo Testing if backend is running...
curl -s "http://localhost:8080/api/auth/test-email-config" > nul
if errorlevel 1 (
    echo ❌ ERROR: Backend is not running on port 8080
    echo Please start the backend first with: mvn spring-boot:run
    echo Or use: start-backend.bat
    pause
    exit /b 1
) else (
    echo ✅ Backend is running
)

echo.
echo Step 1.5: Testing password hash...
echo.

echo Testing password hash for 'admin123':
curl -s "http://localhost:8080/api/auth/test-password-hash?password=admin123"
echo.
echo.

echo Testing your specific hash:
echo Hash: $2a$10$JQnA/KNpUZFVzrPOdAzPmePGp3csnIZXZCyVuIkV9u3oe7yCHp0ya
echo This should show "matchesKnownHash": true if your database hash is correct.
echo.

echo.
echo Step 2: Testing admin login credentials...
echo.

echo Testing login with admin credentials:
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}" ^
  -w "\nHTTP Status: %%{http_code}\n" ^
  -v

echo.
echo.
echo ========================================
echo TROUBLESHOOTING STEPS:
echo ========================================
echo.
echo If login failed, try these steps:
echo.
echo 1. CHECK DATABASE:
echo    - Connect to your MySQL database
echo    - Run: SELECT * FROM users WHERE email = 'admin@tourism.com';
echo    - Verify the admin user exists and enabled = 1
echo.
echo 2. VERIFY PASSWORD HASH:
echo    - The password should be BCrypt hashed
echo    - Hash should start with $2a$10$
echo.
echo 3. RESET ADMIN USER:
echo    - Run the SQL commands shown below
echo.
echo 4. CHECK APPLICATION LOGS:
echo    - Look for authentication errors in backend console
echo.
echo ========================================
echo SQL COMMANDS TO FIX ADMIN USER:
echo ========================================
echo.
echo -- Delete existing admin user if any
echo DELETE FROM users WHERE email = 'admin@tourism.com';
echo.
echo -- Create new admin user with CORRECT hash for 'admin123'
echo INSERT INTO users (first_name, last_name, email, password, enabled, role, created_at) 
echo VALUES ('Admin', 'User', 'admin@tourism.com', 
echo '$2a$10$JQnA/KNpUZFVzrPOdAzPmePGp3csnIZXZCyVuIkV9u3oe7yCHp0ya', 
echo TRUE, 'ADMIN', NOW());
echo.
echo ========================================
echo ALTERNATIVE CREDENTIALS TO TRY:
echo ========================================
echo.
echo If admin@tourism.com doesn't work, try these:
echo.
echo Owner Account:
echo Email: owner@example.com
echo Password: owner123
echo.
echo Guest Account:
echo Email: guest@example.com  
echo Password: guest123
echo.
echo ========================================
echo.
pause
