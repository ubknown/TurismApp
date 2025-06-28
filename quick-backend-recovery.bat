@echo off
echo ========================================
echo QUICK BACKEND RECOVERY
echo ========================================
echo.

echo Step 1: Killing any existing Java processes...
taskkill /f /im java.exe 2>nul
echo Done.
echo.

echo Step 2: Cleaning Maven build...
mvnw.cmd clean
echo.

echo Step 3: Checking for compilation errors...
mvnw.cmd compile
if errorlevel 1 (
    echo ❌ COMPILATION FAILED!
    echo Check the error messages above.
    echo Common fixes:
    echo - Restore corrupted files from Git
    echo - Fix syntax errors in Java files
    echo - Check imports and dependencies
    pause
    exit /b 1
)
echo ✅ Compilation successful!
echo.

echo Step 4: Starting backend...
echo.
echo ========================================
echo BACKEND STARTING - Watch for errors:
echo ========================================
echo.

mvnw.cmd spring-boot:run
