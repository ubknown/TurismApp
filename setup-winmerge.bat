@echo off
echo ========================================
echo WINMERGE SETUP FOR CORRUPTION ANALYSIS
echo ========================================
echo.

REM Check if WinMerge is installed
set WINMERGE_PATH=
if exist "C:\Program Files\WinMerge\WinMergeU.exe" (
    set WINMERGE_PATH=C:\Program Files\WinMerge\WinMergeU.exe
) else if exist "C:\Program Files (x86)\WinMerge\WinMergeU.exe" (
    set WINMERGE_PATH=C:\Program Files (x86)\WinMerge\WinMergeU.exe
) else (
    echo WinMerge not found in standard locations.
    echo.
    echo Please install WinMerge from: https://winmerge.org/
    echo.
    echo Alternative free tools:
    echo 1. Beyond Compare ^(trial^): https://www.scootersoftware.com/
    echo 2. FreeCommander: https://freecommander.com/
    echo 3. Use built-in Windows FC command ^(basic^)
    echo.
    pause
    exit /b
)

echo WinMerge found at: %WINMERGE_PATH%
echo.

REM Set directory paths
set CURRENT_DIR=d:\razu\Licenta\SCD\turismapp
set BACKUP_DIR=d:\razu\Licenta\SCD\turismapp copy

echo Current project: %CURRENT_DIR%
echo Backup project: %BACKUP_DIR%
echo.

REM Verify directories exist
if not exist "%CURRENT_DIR%" (
    echo ERROR: Current directory not found: %CURRENT_DIR%
    echo Please adjust the path in this script.
    pause
    exit /b
)

if not exist "%BACKUP_DIR%" (
    echo ERROR: Backup directory not found: %BACKUP_DIR%
    echo Please adjust the path in this script.
    pause
    exit /b
)

echo Choose comparison option:
echo.
echo 1. Compare entire project folders ^(recommended^)
echo 2. Compare specific critical files only
echo 3. Compare Java source files only
echo 4. Compare frontend files only
echo.
set /p OPTION="Enter your choice (1-4): "

if "%OPTION%"=="1" goto FULL_COMPARE
if "%OPTION%"=="2" goto CRITICAL_COMPARE
if "%OPTION%"=="3" goto JAVA_COMPARE
if "%OPTION%"=="4" goto FRONTEND_COMPARE

echo Invalid option. Defaulting to full comparison.

:FULL_COMPARE
echo.
echo Starting full project comparison...
echo This will open WinMerge to compare the entire project folders.
echo.
echo In WinMerge:
echo - Green files = Identical
echo - Yellow files = Different ^(need restoration^)
echo - Red files = Missing in one folder
echo - Gray files = Binary differences
echo.
echo Right-click on different files and select "Copy to Left" to restore from backup.
echo.
pause
"%WINMERGE_PATH%" "%CURRENT_DIR%" "%BACKUP_DIR%"
goto END

:CRITICAL_COMPARE
echo.
echo Comparing critical files individually...
echo.

set CRITICAL_FILES[0]=src\main\java\com\licentarazu\turismapp\controller\AuthController.java
set CRITICAL_FILES[1]=src\main\resources\application.properties
set CRITICAL_FILES[2]=pom.xml
set CRITICAL_FILES[3]=New front\package.json
set CRITICAL_FILES[4]=New front\src\App.jsx

for /l %%i in (0,1,4) do (
    set CURRENT_FILE=%CURRENT_DIR%\!CRITICAL_FILES[%%i]!
    set BACKUP_FILE=%BACKUP_DIR%\!CRITICAL_FILES[%%i]!
    
    if exist "!CURRENT_FILE!" (
        if exist "!BACKUP_FILE!" (
            echo Comparing: !CRITICAL_FILES[%%i]!
            "%WINMERGE_PATH%" "!CURRENT_FILE!" "!BACKUP_FILE!"
            echo.
            set /p CONTINUE="Press Enter to continue to next file, or 'q' to quit: "
            if /i "!CONTINUE!"=="q" goto END
        ) else (
            echo Backup file missing: !CRITICAL_FILES[%%i]!
        )
    ) else (
        echo Current file missing: !CRITICAL_FILES[%%i]!
        if exist "!BACKUP_FILE!" (
            echo Opening backup file for reference...
            "%WINMERGE_PATH%" "!BACKUP_FILE!"
        )
    )
)
goto END

:JAVA_COMPARE
echo.
echo Comparing Java source directory...
set JAVA_CURRENT=%CURRENT_DIR%\src\main\java
set JAVA_BACKUP=%BACKUP_DIR%\src\main\java

if exist "%JAVA_CURRENT%" (
    if exist "%JAVA_BACKUP%" (
        "%WINMERGE_PATH%" "%JAVA_CURRENT%" "%JAVA_BACKUP%"
    ) else (
        echo Java backup directory not found!
    )
) else (
    echo Java current directory not found!
)
goto END

:FRONTEND_COMPARE
echo.
echo Comparing frontend directory...
set FRONTEND_CURRENT=%CURRENT_DIR%\New front\src
set FRONTEND_BACKUP=%BACKUP_DIR%\New front\src

if exist "%FRONTEND_CURRENT%" (
    if exist "%FRONTEND_BACKUP%" (
        "%WINMERGE_PATH%" "%FRONTEND_CURRENT%" "%FRONTEND_BACKUP%"
    ) else (
        echo Frontend backup directory not found!
    )
) else (
    echo Frontend current directory not found!
)
goto END

:END
echo.
echo ========================================
echo COMPARISON COMPLETE
echo ========================================
echo.
echo Next steps:
echo 1. Review the differences found in WinMerge
echo 2. For different files, right-click and "Copy to Left" to restore
echo 3. Run the automated restoration script: restore-from-backup.bat
echo 4. Test your application after restoration
echo.
pause
