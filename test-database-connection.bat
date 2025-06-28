@echo off
echo Testing Spring Boot Database Connection...
echo.

cd /d "D:\razu\Licenta\SCD\TurismApp"

echo Starting Spring Boot application...
echo Press Ctrl+C to stop the application after it starts
echo.

mvnw.cmd spring-boot:run

echo.
echo Application stopped.
pause
