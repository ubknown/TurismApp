@echo off
echo.
echo ==========================================
echo Testing Backend with New DataSeeder
echo ==========================================
echo.

echo Starting backend with new realistic data seeder...
echo This will:
echo  - Create 10 realistic guest users
echo  - Create 5 property owners
echo  - Create 19 accommodation units across Romania
echo  - Create 28 realistic reservations
echo.

cd /d "d:\razu\Licenta\SCD\TurismApp"

echo Building and starting backend...
mvnw.cmd clean spring-boot:run

pause
