@echo off
echo ========================================
echo FIXING OWNER APPLICATIONS TABLE SCHEMA
echo ========================================
echo.

echo This script will fix the owner_applications table to allow NULL user_id
echo which is required for proper account deletion functionality.
echo.

echo Database: turismdb
echo Table: owner_applications
echo Fix: Make user_id column nullable
echo.

set /p DB_PASSWORD="Enter MySQL root password: "

echo.
echo Applying owner applications migration...
echo.

mysql -u root -p%DB_PASSWORD% turismdb < owner_applications_migration.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ SUCCESS: Migration applied successfully!
    echo.
    echo The owner_applications table now allows:
    echo - NULL user_id (preserves applications after account deletion)
    echo - Email-based tracking for duplicate prevention
    echo.
    echo You can now delete user accounts without database errors.
) else (
    echo.
    echo ❌ ERROR: Migration failed!
    echo Please check the error messages above.
    echo.
    echo Common issues:
    echo 1. Wrong MySQL password
    echo 2. Database turismdb doesn't exist
    echo 3. Migration already applied
)

echo.
pause
