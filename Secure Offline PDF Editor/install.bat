@echo off
echo ========================================
echo Secure PDF Editor Installer
echo ========================================
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running as Administrator - Good!
) else (
    echo WARNING: Not running as Administrator
    echo Some features may not work properly
    echo.
)

REM Create installation directory
set INSTALL_DIR=%ProgramFiles%\SecurePDFEditor
echo Installing to: %INSTALL_DIR%

if not exist "%INSTALL_DIR%" (
    mkdir "%INSTALL_DIR%"
    echo Created installation directory
) else (
    echo Installation directory already exists
)

REM Copy application files
echo.
echo Copying application files...
copy "build\libs\secure-offline-pdf-editor-1.0.0.jar" "%INSTALL_DIR%\SecurePDFEditor.jar"
if %errorLevel% == 0 (
    echo ✓ Application JAR copied successfully
) else (
    echo ✗ Failed to copy application JAR
    pause
    exit /b 1
)

REM Copy resources
if exist "src\main\resources" (
    xcopy "src\main\resources" "%INSTALL_DIR%\resources\" /E /I /Y >nul
    echo ✓ Resources copied successfully
)

REM Create desktop shortcut
echo.
echo Creating desktop shortcut...
set DESKTOP=%USERPROFILE%\Desktop
set SHORTCUT=%DESKTOP%\Secure PDF Editor.lnk

echo @echo off > "%TEMP%\create_shortcut.vbs"
echo Set oWS = WScript.CreateObject("WScript.Shell") >> "%TEMP%\create_shortcut.vbs"
echo sLinkFile = "%SHORTCUT%" >> "%TEMP%\create_shortcut.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%TEMP%\create_shortcut.vbs"
echo oLink.TargetPath = "java" >> "%TEMP%\create_shortcut.vbs"
echo oLink.Arguments = "-jar ""%INSTALL_DIR%\SecurePDFEditor.jar""" >> "%TEMP%\create_shortcut.vbs"
echo oLink.WorkingDirectory = "%INSTALL_DIR%" >> "%TEMP%\create_shortcut.vbs"
echo oLink.Description = "Secure Offline PDF Editor" >> "%TEMP%\create_shortcut.vbs"
echo oLink.Save >> "%TEMP%\create_shortcut.vbs"

cscript //nologo "%TEMP%\create_shortcut.vbs"
if %errorLevel% == 0 (
    echo ✓ Desktop shortcut created
) else (
    echo ✗ Failed to create desktop shortcut
)

REM Create start menu shortcut
echo Creating start menu shortcut...
set START_MENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs
set START_MENU_SHORTCUT=%START_MENU%\Secure PDF Editor.lnk

echo @echo off > "%TEMP%\create_startmenu_shortcut.vbs"
echo Set oWS = WScript.CreateObject("WScript.Shell") >> "%TEMP%\create_startmenu_shortcut.vbs"
echo sLinkFile = "%START_MENU_SHORTCUT%" >> "%TEMP%\create_startmenu_shortcut.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%TEMP%\create_startmenu_shortcut.vbs"
echo oLink.TargetPath = "java" >> "%TEMP%\create_startmenu_shortcut.vbs"
echo oLink.Arguments = "-jar ""%INSTALL_DIR%\SecurePDFEditor.jar""" >> "%TEMP%\create_startmenu_shortcut.vbs"
echo oLink.WorkingDirectory = "%INSTALL_DIR%" >> "%TEMP%\create_startmenu_shortcut.vbs"
echo oLink.Description = "Secure Offline PDF Editor" >> "%TEMP%\create_startmenu_shortcut.vbs"
echo oLink.Save >> "%TEMP%\create_startmenu_shortcut.vbs"

cscript //nologo "%TEMP%\create_startmenu_shortcut.vbs"
if %errorLevel% == 0 (
    echo ✓ Start menu shortcut created
) else (
    echo ✗ Failed to create start menu shortcut
)

REM Create uninstaller
echo.
echo Creating uninstaller...
echo @echo off > "%INSTALL_DIR%\uninstall.bat"
echo echo Uninstalling Secure PDF Editor... >> "%INSTALL_DIR%\uninstall.bat"
echo echo. >> "%INSTALL_DIR%\uninstall.bat"
echo del "%DESKTOP%\Secure PDF Editor.lnk" >> "%INSTALL_DIR%\uninstall.bat"
echo del "%START_MENU%\Secure PDF Editor.lnk" >> "%INSTALL_DIR%\uninstall.bat"
echo rmdir /s /q "%INSTALL_DIR%" >> "%INSTALL_DIR%\uninstall.bat"
echo echo Uninstallation complete. >> "%INSTALL_DIR%\uninstall.bat"
echo pause >> "%INSTALL_DIR%\uninstall.bat"

echo ✓ Uninstaller created

REM Create registry entries for uninstall
echo.
echo Creating registry entries...
reg add "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\SecurePDFEditor" /v "DisplayName" /t REG_SZ /d "Secure PDF Editor" /f
reg add "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\SecurePDFEditor" /v "UninstallString" /t REG_SZ /d "%INSTALL_DIR%\uninstall.bat" /f
reg add "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\SecurePDFEditor" /v "DisplayVersion" /t REG_SZ /d "1.0.0" /f
reg add "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\SecurePDFEditor" /v "Publisher" /t REG_SZ /d "SecurePDFEditor" /f
reg add "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\SecurePDFEditor" /v "InstallLocation" /t REG_SZ /d "%INSTALL_DIR%" /f

echo ✓ Registry entries created

REM Clean up temporary files
del "%TEMP%\create_shortcut.vbs" >nul 2>&1
del "%TEMP%\create_startmenu_shortcut.vbs" >nul 2>&1

echo.
echo ========================================
echo Installation Complete!
echo ========================================
echo.
echo Secure PDF Editor has been installed to:
echo %INSTALL_DIR%
echo.
echo You can now:
echo - Double-click the desktop shortcut to launch
echo - Find it in the Start Menu under "Secure PDF Editor"
echo - Uninstall using: %INSTALL_DIR%\uninstall.bat
echo.
echo Requirements:
echo - Java 21 or later must be installed
echo - Run as Administrator for full functionality
echo.
pause

