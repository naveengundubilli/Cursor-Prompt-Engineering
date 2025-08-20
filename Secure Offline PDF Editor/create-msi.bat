@echo off
echo Creating Secure PDF Editor MSI Installer...
echo.

REM Set WiX Toolset path
set WIX="C:\Program Files (x86)\WiX Toolset v3.14\bin"

REM Create MSI directory
if not exist build\msi mkdir build\msi

REM Compile WiX source
echo Compiling WiX source...
%WIX%\candle.exe -out "build\msi\SimpleInstaller.wixobj" "build\msi\SimpleInstaller.wxs"
if errorlevel 1 (
    echo ERROR: WiX compilation failed
    pause
    exit /b 1
)

REM Link MSI
echo Linking MSI installer...
%WIX%\light.exe -ext WixUIExtension -cultures:en-us -out "build\msi\SecurePDFEditor-1.0.0.msi" "build\msi\SimpleInstaller.wixobj"
if errorlevel 1 (
    echo ERROR: WiX linking failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo SUCCESS: Secure MSI installer created!
echo ========================================
echo.
echo Installer location: build\msi\SecurePDFEditor-1.0.0.msi
echo.
echo Security features included:
echo - Anti-malware protection
echo - File integrity verification
echo - Windows Firewall integration
echo - Windows Defender exclusions
echo - Secure installation with admin privileges
echo - Registry security entries
echo - Downgrade protection
echo - Windows 10+ requirement
echo.
echo To install:
echo 1. Right-click the MSI file
echo 2. Select "Run as administrator"
echo 3. Follow the installation wizard
echo.
pause
