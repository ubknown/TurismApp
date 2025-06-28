@echo off
echo ========================================
echo BACKEND STARTUP DIAGNOSTIC
echo ========================================
echo.

echo Attempting to start Spring Boot backend...
echo This will show any compilation or startup errors.
echo.

echo Current directory: %CD%
echo.

echo Starting with Maven Wrapper...
echo.

echo ========================================
echo IF THIS FAILS, LOOK FOR THESE ERRORS:
echo ========================================
echo.
echo COMPILATION ERRORS:
echo - "Compilation failure" = Java syntax errors
echo - "cannot find symbol" = Missing imports/variables
echo - "class, interface, enum, or record expected" = Syntax errors
echo.
echo STARTUP ERRORS: 
echo - "Port 8080 was already in use" = Port conflict
echo - "Unable to start web server" = Configuration issue
echo - "Failed to configure a DataSource" = Database connection issue
echo - "ClassNotFoundException" = Missing dependencies
echo.
echo ========================================
echo.

mvnw.cmd spring-boot:run
