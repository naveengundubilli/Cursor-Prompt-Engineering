@echo off
echo ========================================
echo Secure Family PDF Software Launcher
echo ========================================
echo.

REM Check if running as administrator (needed for Norton exclusions)
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running as administrator - Norton exclusions will be applied
    goto :add_exclusions
) else (
    echo Running as regular user - Norton exclusions may not be applied
    goto :run_app
)

:add_exclusions
echo.
echo Adding Norton Antivirus exclusions...
echo.

REM Add Norton exclusions for the application directory
powershell -Command "& {Add-MpPreference -ExclusionPath '%~dp0' -Force}" 2>nul
if %errorLevel% == 0 (
    echo ✓ Norton exclusion added for application directory
) else (
    echo - Norton exclusion failed (may not be Norton or already excluded)
)

REM Add exclusion for the specific executable
powershell -Command "& {Add-MpPreference -ExclusionPath '%~dp0SecureFamilyPdf.exe' -Force}" 2>nul
if %errorLevel% == 0 (
    echo ✓ Norton exclusion added for SecureFamilyPdf.exe
) else (
    echo - Norton exclusion failed (may not be Norton or already excluded)
)

echo.

:run_app
echo Starting Secure Family PDF Software...
echo.

REM Run the application
start "" "%~dp0SecureFamilyPdf.exe"

echo Application launched successfully!
echo.
echo If you encounter any issues:
echo 1. Right-click SecureFamilyPdf.exe and select "Run as administrator"
echo 2. Temporarily disable your antivirus software
echo 3. Add the application folder to your antivirus exclusions
echo.
pause

