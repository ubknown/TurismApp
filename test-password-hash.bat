@echo off
echo ========================================
echo PASSWORD HASH TESTER
echo ========================================
echo.
echo This will test the password hash for admin123
echo.

echo Compiling and running password hash generator...
echo.

REM Use Maven to compile and run the utility with proper classpath
mvn compile exec:java -Dexec.mainClass="com.licentarazu.turismapp.util.PasswordHashGenerator" -Dexec.classpathScope="compile" -q

echo.
echo ========================================
echo.
pause
