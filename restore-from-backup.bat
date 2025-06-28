@echo off
setlocal enabledelayedexpansion

echo ========================================
echo SELECTIVE FILE RESTORATION TOOL
echo ========================================
echo.

REM Set paths (adjust these if needed)
set CURRENT_DIR=d:\razu\Licenta\SCD\turismapp
set BACKUP_DIR=d:\razu\Licenta\SCD\turismapp copy
set RESTORE_LOG=d:\razu\Licenta\SCD\TurismApp\restoration_log.txt

echo This script will help you restore corrupted files from backup.
echo.
echo WARNING: This will overwrite files in your current project!
echo Make sure you have reviewed the analysis reports first.
echo.
set /p CONFIRM="Do you want to proceed? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo Operation cancelled.
    pause
    exit /b
)

echo.
echo Starting restoration log...
echo Restoration Log - %date% %time% > "%RESTORE_LOG%"
echo ================================== >> "%RESTORE_LOG%"
echo. >> "%RESTORE_LOG%"

echo.
echo Phase 1: Critical Java Files
echo =============================

set JAVA_FILES_RESTORED=0

REM List of critical Java files to restore
set JAVA_FILES[0]=src\main\java\com\licentarazu\turismapp\controller\AuthController.java
set JAVA_FILES[1]=src\main\java\com\licentarazu\turismapp\service\UserService.java
set JAVA_FILES[2]=src\main\java\com\licentarazu\turismapp\service\EmailService.java
set JAVA_FILES[3]=src\main\java\com\licentarazu\turismapp\config\SecurityConfig.java
set JAVA_FILES[4]=src\main\java\com\licentarazu\turismapp\repository\UserRepository.java
set JAVA_FILES[5]=src\main\java\com\licentarazu\turismapp\dto\LoginRequest.java
set JAVA_FILES[6]=src\main\java\com\licentarazu\turismapp\dto\RegisterRequest.java

for /l %%i in (0,1,6) do (
    set CURRENT_FILE=%CURRENT_DIR%\!JAVA_FILES[%%i]!
    set BACKUP_FILE=%BACKUP_DIR%\!JAVA_FILES[%%i]!
    
    if exist "!BACKUP_FILE!" (
        if exist "!CURRENT_FILE!" (
            fc "!CURRENT_FILE!" "!BACKUP_FILE!" > nul 2>&1
            if !errorlevel! neq 0 (
                echo Restoring: !JAVA_FILES[%%i]!
                copy "!BACKUP_FILE!" "!CURRENT_FILE!" > nul
                echo RESTORED: !JAVA_FILES[%%i]! >> "%RESTORE_LOG%"
                set /a JAVA_FILES_RESTORED+=1
            ) else (
                echo Identical: !JAVA_FILES[%%i]!
                echo IDENTICAL: !JAVA_FILES[%%i]! >> "%RESTORE_LOG%"
            )
        ) else (
            echo Missing file, restoring: !JAVA_FILES[%%i]!
            REM Create directory if needed
            for %%F in ("!CURRENT_FILE!") do (
                if not exist "%%~dpF" mkdir "%%~dpF"
            )
            copy "!BACKUP_FILE!" "!CURRENT_FILE!" > nul
            echo RESTORED: !JAVA_FILES[%%i]! ^(was missing^) >> "%RESTORE_LOG%"
            set /a JAVA_FILES_RESTORED+=1
        )
    ) else (
        echo Warning: !JAVA_FILES[%%i]! not found in backup!
        echo WARNING: !JAVA_FILES[%%i]! not found in backup >> "%RESTORE_LOG%"
    )
)

echo.
echo Phase 2: Configuration Files
echo ============================

set CONFIG_FILES_RESTORED=0

set CONFIG_FILES[0]=src\main\resources\application.properties
set CONFIG_FILES[1]=pom.xml
set CONFIG_FILES[2]=src\main\resources\static\*
set CONFIG_FILES[3]=src\main\resources\templates\*

for /l %%i in (0,1,1) do (
    set CURRENT_FILE=%CURRENT_DIR%\!CONFIG_FILES[%%i]!
    set BACKUP_FILE=%BACKUP_DIR%\!CONFIG_FILES[%%i]!
    
    if exist "!BACKUP_FILE!" (
        if exist "!CURRENT_FILE!" (
            fc "!CURRENT_FILE!" "!BACKUP_FILE!" > nul 2>&1
            if !errorlevel! neq 0 (
                echo Restoring: !CONFIG_FILES[%%i]!
                copy "!BACKUP_FILE!" "!CURRENT_FILE!" > nul
                echo RESTORED: !CONFIG_FILES[%%i]! >> "%RESTORE_LOG%"
                set /a CONFIG_FILES_RESTORED+=1
            ) else (
                echo Identical: !CONFIG_FILES[%%i]!
                echo IDENTICAL: !CONFIG_FILES[%%i]! >> "%RESTORE_LOG%"
            )
        ) else (
            echo Missing file, restoring: !CONFIG_FILES[%%i]!
            for %%F in ("!CURRENT_FILE!") do (
                if not exist "%%~dpF" mkdir "%%~dpF"
            )
            copy "!BACKUP_FILE!" "!CURRENT_FILE!" > nul
            echo RESTORED: !CONFIG_FILES[%%i]! ^(was missing^) >> "%RESTORE_LOG%"
            set /a CONFIG_FILES_RESTORED+=1
        )
    ) else (
        echo Warning: !CONFIG_FILES[%%i]! not found in backup!
        echo WARNING: !CONFIG_FILES[%%i]! not found in backup >> "%RESTORE_LOG%"
    )
)

echo.
echo Phase 3: Frontend Files
echo =======================

set FRONTEND_FILES_RESTORED=0

set FRONTEND_FILES[0]=New front\package.json
set FRONTEND_FILES[1]=New front\src\App.jsx
set FRONTEND_FILES[2]=New front\src\services\api.js
set FRONTEND_FILES[3]=New front\src\services\authService.js
set FRONTEND_FILES[4]=New front\src\main.jsx
set FRONTEND_FILES[5]=New front\vite.config.js

for /l %%i in (0,1,5) do (
    set CURRENT_FILE=%CURRENT_DIR%\!FRONTEND_FILES[%%i]!
    set BACKUP_FILE=%BACKUP_DIR%\!FRONTEND_FILES[%%i]!
    
    if exist "!BACKUP_FILE!" (
        if exist "!CURRENT_FILE!" (
            fc "!CURRENT_FILE!" "!BACKUP_FILE!" > nul 2>&1
            if !errorlevel! neq 0 (
                echo Restoring: !FRONTEND_FILES[%%i]!
                copy "!BACKUP_FILE!" "!CURRENT_FILE!" > nul
                echo RESTORED: !FRONTEND_FILES[%%i]! >> "%RESTORE_LOG%"
                set /a FRONTEND_FILES_RESTORED+=1
            ) else (
                echo Identical: !FRONTEND_FILES[%%i]!
                echo IDENTICAL: !FRONTEND_FILES[%%i]! >> "%RESTORE_LOG%"
            )
        ) else (
            echo Missing file, restoring: !FRONTEND_FILES[%%i]!
            for %%F in ("!CURRENT_FILE!") do (
                if not exist "%%~dpF" mkdir "%%~dpF"
            )
            copy "!BACKUP_FILE!" "!CURRENT_FILE!" > nul
            echo RESTORED: !FRONTEND_FILES[%%i]! ^(was missing^) >> "%RESTORE_LOG%"
            set /a FRONTEND_FILES_RESTORED+=1
        )
    ) else (
        echo Warning: !FRONTEND_FILES[%%i]! not found in backup!
        echo WARNING: !FRONTEND_FILES[%%i]! not found in backup >> "%RESTORE_LOG%"
    )
)

echo.
echo ========================================
echo RESTORATION SUMMARY
echo ========================================
echo.
echo Java files restored: %JAVA_FILES_RESTORED%
echo Config files restored: %CONFIG_FILES_RESTORED%
echo Frontend files restored: %FRONTEND_FILES_RESTORED%
echo.
set /a TOTAL_RESTORED=%JAVA_FILES_RESTORED%+%CONFIG_FILES_RESTORED%+%FRONTEND_FILES_RESTORED%
echo Total files restored: %TOTAL_RESTORED%
echo.
echo Restoration completed: %date% %time% >> "%RESTORE_LOG%"
echo Total files restored: %TOTAL_RESTORED% >> "%RESTORE_LOG%"

if %TOTAL_RESTORED% gtr 0 (
    echo.
    echo NEXT STEPS:
    echo 1. Clean and rebuild the project: mvnw.cmd clean compile
    echo 2. Start the backend: mvnw.cmd spring-boot:run
    echo 3. Test frontend connectivity
    echo 4. Verify all authentication endpoints work
    echo.
    echo Would you like to run the build now?
    set /p BUILD_NOW="Run 'mvnw.cmd clean compile' now? (Y/N): "
    if /i "!BUILD_NOW!"=="Y" (
        echo.
        echo Building project...
        cd "%CURRENT_DIR%"
        .\mvnw.cmd clean compile
    )
)

echo.
echo Full restoration log saved to: %RESTORE_LOG%
pause
