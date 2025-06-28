@echo off
echo Starting TurismApp Backend with Environment Variables...

REM Set environment variables from .env file values
set DB_URL=jdbc:mysql://localhost:3306/turismdb
set DB_USERNAME=root
set DB_PASSWORD=Rzvtare112
set JWT_SECRET=mySecretKeyForJWTTokenGenerationDiplomaPresentationSecure2025
set JWT_EXPIRATION=86400000
set EMAIL_USERNAME=turismapplic@gmail.com
set EMAIL_PASSWORD=yktw fpad qjrw mzmv
set EMAIL_DEBUG=false
set EMAIL_HOST=smtp.gmail.com
set EMAIL_PORT=587
set EMAIL_SMTP_TRUST=smtp.gmail.com

echo Environment variables set successfully!
echo Starting Spring Boot application...

REM Start the application
mvnw.cmd spring-boot:run

pause
