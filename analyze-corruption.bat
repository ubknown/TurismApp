@echo off
setlocal enabledelayedexpansion

echo ========================================
echo TURISMAPP CORRUPTION ANALYSIS TOOL
echo ========================================
echo.

REM Set paths (adjust these if needed)
set CURRENT_DIR=d:\razu\Licenta\SCD\turismapp
set BACKUP_DIR=d:\razu\Licenta\SCD\turismapp copy
set REPORT_DIR=d:\razu\Licenta\SCD\TurismApp\corruption_analysis

REM Create report directory
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"

echo Step 1: Generating file inventories...
echo.

REM Generate file lists
dir "%CURRENT_DIR%" /s /b /a-d > "%REPORT_DIR%\current_files.txt"
dir "%BACKUP_DIR%" /s /b /a-d > "%REPORT_DIR%\backup_files.txt"

REM Generate detailed lists with sizes and dates
dir "%CURRENT_DIR%" /s /a-d > "%REPORT_DIR%\current_detailed.txt"
dir "%BACKUP_DIR%" /s /a-d > "%REPORT_DIR%\backup_detailed.txt"

echo Step 2: Comparing file lists...
echo.

REM Compare file lists
fc "%REPORT_DIR%\current_files.txt" "%REPORT_DIR%\backup_files.txt" > "%REPORT_DIR%\file_list_comparison.txt" 2>&1

echo Step 3: Analyzing file differences...
echo.

REM Create a list of files to check
set TEMP_FILE=%REPORT_DIR%\files_to_check.txt
> "%TEMP_FILE%" (
    echo src\main\java\com\licentarazu\turismapp\controller\AuthController.java
    echo src\main\java\com\licentarazu\turismapp\service\UserService.java
    echo src\main\java\com\licentarazu\turismapp\service\EmailService.java
    echo src\main\java\com\licentarazu\turismapp\config\SecurityConfig.java
    echo src\main\resources\application.properties
    echo pom.xml
    echo New front\package.json
    echo New front\src\App.jsx
    echo New front\src\services\api.js
    echo New front\src\services\authService.js
)

REM Compare critical files
set DIFF_REPORT=%REPORT_DIR%\critical_file_differences.txt
echo Critical File Comparison Report > "%DIFF_REPORT%"
echo ================================== >> "%DIFF_REPORT%"
echo. >> "%DIFF_REPORT%"

for /f "tokens=*" %%F in (%TEMP_FILE%) do (
    set CURRENT_FILE=%CURRENT_DIR%\%%F
    set BACKUP_FILE=%BACKUP_DIR%\%%F
    
    echo Checking: %%F >> "%DIFF_REPORT%"
    echo ---------------------------------------- >> "%DIFF_REPORT%"
    
    if exist "!CURRENT_FILE!" (
        if exist "!BACKUP_FILE!" (
            fc "!CURRENT_FILE!" "!BACKUP_FILE!" > nul 2>&1
            if !errorlevel! equ 0 (
                echo STATUS: IDENTICAL >> "%DIFF_REPORT%"
            ) else (
                echo STATUS: DIFFERENT ^(NEEDS RESTORATION^) >> "%DIFF_REPORT%"
                echo File sizes: >> "%DIFF_REPORT%"
                for %%A in ("!CURRENT_FILE!") do echo   Current: %%~zA bytes >> "%DIFF_REPORT%"
                for %%A in ("!BACKUP_FILE!") do echo   Backup:  %%~zA bytes >> "%DIFF_REPORT%"
            )
        ) else (
            echo STATUS: MISSING IN BACKUP ^(EXTRA FILE^) >> "%DIFF_REPORT%"
        )
    ) else (
        if exist "!BACKUP_FILE!" (
            echo STATUS: MISSING IN CURRENT ^(NEEDS RESTORATION^) >> "%DIFF_REPORT%"
        ) else (
            echo STATUS: MISSING IN BOTH >> "%DIFF_REPORT%"
        )
    )
    echo. >> "%DIFF_REPORT%"
)

echo Step 4: Generating size comparison...
echo.

REM Compare total sizes
set SIZE_REPORT=%REPORT_DIR%\size_comparison.txt
echo Directory Size Comparison > "%SIZE_REPORT%"
echo ========================= >> "%SIZE_REPORT%"
echo. >> "%SIZE_REPORT%"

echo Current Directory: >> "%SIZE_REPORT%"
dir "%CURRENT_DIR%" /s | find "File(s)" >> "%SIZE_REPORT%"
echo. >> "%SIZE_REPORT%"

echo Backup Directory: >> "%SIZE_REPORT%"
dir "%BACKUP_DIR%" /s | find "File(s)" >> "%SIZE_REPORT%"
echo. >> "%SIZE_REPORT%"

echo Step 5: Creating recovery recommendations...
echo.

set RECOVERY_REPORT=%REPORT_DIR%\recovery_recommendations.txt
echo RECOVERY RECOMMENDATIONS > "%RECOVERY_REPORT%"
echo ======================== >> "%RECOVERY_REPORT%"
echo. >> "%RECOVERY_REPORT%"
echo Based on the analysis above, here are the recommended actions: >> "%RECOVERY_REPORT%"
echo. >> "%RECOVERY_REPORT%"
echo 1. Review the critical_file_differences.txt report >> "%RECOVERY_REPORT%"
echo 2. For files marked as DIFFERENT or MISSING IN CURRENT, copy from backup >> "%RECOVERY_REPORT%"
echo 3. Priority order for restoration: >> "%RECOVERY_REPORT%"
echo    - Java source files ^(.java^) >> "%RECOVERY_REPORT%"
echo    - Configuration files ^(.properties, .xml, .yml^) >> "%RECOVERY_REPORT%"
echo    - Frontend source files ^(.jsx, .js, .css^) >> "%RECOVERY_REPORT%"
echo    - Documentation files ^(.md^) >> "%RECOVERY_REPORT%"
echo. >> "%RECOVERY_REPORT%"
echo 4. After restoration, rebuild and test the application >> "%RECOVERY_REPORT%"
echo. >> "%RECOVERY_REPORT%"

echo ========================================
echo ANALYSIS COMPLETE!
echo ========================================
echo.
echo Reports generated in: %REPORT_DIR%
echo.
echo Key files to review:
echo 1. critical_file_differences.txt - Shows which critical files need restoration
echo 2. file_list_comparison.txt - Full file list differences
echo 3. size_comparison.txt - Directory size comparison
echo 4. recovery_recommendations.txt - Step-by-step recovery plan
echo.
echo Next steps:
echo 1. Review the reports above
echo 2. Use WinMerge for visual comparison ^(recommended^)
echo 3. Run the selective restoration script
echo.

echo Opening report directory...
explorer "%REPORT_DIR%"

pause
