@echo off
echo ========================================
echo SIMPLE BCRYPT HASH GENERATOR
echo ========================================
echo.

echo Generating BCrypt hash for 'admin123'...
echo.

REM Method 1: Using Maven to run with proper classpath
echo Using Maven to compile and run:
mvn compile exec:java -Dexec.mainClass="BCryptHasher" -Dexec.args="" -q 2>nul

if errorlevel 1 (
    echo Maven approach failed, trying alternative...
    echo.
    
    REM Method 2: Manual compilation with Maven dependencies
    echo Compiling with Maven classpath...
    
    REM Find the Spring Security JAR in Maven repository
    set SPRING_JAR=%USERPROFILE%\.m2\repository\org\springframework\security\spring-security-crypto\6.1.0\spring-security-crypto-6.1.0.jar
    
    if exist "%SPRING_JAR%" (
        echo Found Spring Security JAR: %SPRING_JAR%
        javac -cp "%SPRING_JAR%" BCryptHasher.java
        if not errorlevel 1 (
            java -cp ".;%SPRING_JAR%" BCryptHasher
        ) else (
            echo Compilation failed!
        )
    ) else (
        echo Spring Security JAR not found at expected location.
        echo Please ensure Maven dependencies are downloaded.
        echo Run: mvn dependency:resolve
    )
)

echo.
echo ========================================
echo.
pause
