@echo off
echo ========================================
echo DATABASE FIX FOR USER DELETION
echo ========================================
echo.

echo This script will fix the owner_applications table to allow user deletion
echo by making the user_id column nullable.
echo.

set /p DB_PASSWORD="Enter MySQL root password: "

echo.
echo Applying database fix...
echo.

mysql -u root -p%DB_PASSWORD% -e "USE turismdb; ALTER TABLE owner_applications MODIFY COLUMN user_id bigint NULL;"

if %ERRORLEVEL% EQU 0 (
    echo ========================================
    echo SUCCESS: Database fix applied!
    echo ========================================
    echo.
    echo The user_id column in owner_applications is now nullable.
    echo You can now delete user accounts without database errors.
    echo.
    echo Testing the fix...
    mysql -u root -p%DB_PASSWORD% -e "USE turismdb; DESCRIBE owner_applications;" | findstr user_id
    echo.
    echo If you see 'YES' in the Null column for user_id, the fix was successful.
) else (
    echo ========================================
    echo ERROR: Failed to apply database fix
    echo ========================================
    echo.
    echo Please check:
    echo 1. MySQL is running
    echo 2. Password is correct
    echo 3. Database 'turismdb' exists
)

echo.
pause
