@echo off
echo ========================================
echo RESTORE PROJECT TO GITHUB MAIN BRANCH
echo ========================================
echo.

echo ⚠️  WARNING: This will discard ALL local changes and restore to GitHub main branch
echo.
echo This will:
echo - Remove all uncommitted changes
echo - Delete new experimental files
echo - Reset to exact GitHub repository state
echo - Cannot be undone without backup
echo.
set /p CONFIRM="Are you sure you want to continue? (type YES to confirm): "

if not "%CONFIRM%"=="YES" (
    echo Operation cancelled.
    pause
    exit /b
)

echo.
echo ========================================
echo STEP 1: BACKUP CURRENT STATE (OPTIONAL)
echo ========================================
echo.
echo Creating backup of current state before reset...
set BACKUP_DIR=backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set BACKUP_DIR=%BACKUP_DIR: =0%
xcopy /E /I /H /Y . "d:\razu\Licenta\SCD\%BACKUP_DIR%" >nul 2>&1
echo Backup created at: d:\razu\Licenta\SCD\%BACKUP_DIR%
echo.

echo ========================================
echo STEP 2: FETCH LATEST FROM GITHUB
echo ========================================
echo.
git fetch origin main
if %ERRORLEVEL% neq 0 (
    echo ❌ Error fetching from GitHub. Check internet connection.
    pause
    exit /b
)
echo ✅ Latest changes fetched from GitHub
echo.

echo ========================================
echo STEP 3: RESET TO GITHUB MAIN BRANCH
echo ========================================
echo.
echo Discarding all local changes...
git reset --hard origin/main
if %ERRORLEVEL% neq 0 (
    echo ❌ Error resetting to main branch
    pause
    exit /b
)
echo ✅ Reset to GitHub main branch completed
echo.

echo ========================================
echo STEP 4: CLEAN UNTRACKED FILES
echo ========================================
echo.
echo Removing untracked files and directories...
git clean -fd
if %ERRORLEVEL% neq 0 (
    echo ❌ Error cleaning untracked files
    pause
    exit /b
)
echo ✅ Untracked files removed
echo.

echo ========================================
echo STEP 5: VERIFY RESTORATION
echo ========================================
echo.
echo Current branch and status:
git branch
echo.
git status
echo.

echo ========================================
echo RESTORATION COMPLETE
echo ========================================
echo.
echo ✅ Project restored to exact GitHub main branch state
echo ✅ All experimental changes removed
echo ✅ Codebase now matches repository
echo.
echo Backup location: d:\razu\Licenta\SCD\%BACKUP_DIR%
echo.
echo Next steps:
echo 1. Verify the application builds and runs correctly
echo 2. Check that all features work as expected
echo 3. If needed, you can reference the backup for any important changes
echo.
pause
