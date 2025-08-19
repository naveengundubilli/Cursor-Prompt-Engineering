@echo off
REM Secure PDF Editor MSI Builder
REM This script builds a secure MSI installer with anti-malware protection

echo ========================================
echo Secure PDF Editor MSI Builder
echo ========================================
echo.

REM Check if Java 21 is installed
echo Checking Java version...
java -version 2>nul
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 21 from: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

REM Check if WiX Toolset is installed
echo Checking WiX Toolset...
where candle.exe >nul 2>&1
if errorlevel 1 (
    echo WARNING: WiX Toolset not found in PATH
    echo Please install WiX Toolset v3.11.2 from: https://github.com/wixtoolset/wix3/releases
    echo Or set WIX environment variable to WiX installation directory
    echo.
    echo Attempting to use default WiX installation path...
)

REM Clean previous builds
echo Cleaning previous builds...
if exist build\msi (
    rmdir /s /q build\msi
)

REM Run tests to ensure security features work
echo Running security tests...
call gradlew.bat test --tests "*SecurityServiceTest*"
if errorlevel 1 (
    echo ERROR: Security tests failed
    pause
    exit /b 1
)

REM Build the application
echo Building application...
call gradlew.bat clean build
if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

REM Create shadow JAR (fat JAR with all dependencies)
echo Creating shadow JAR...
call gradlew.bat shadowJar
if errorlevel 1 (
    echo ERROR: Shadow JAR creation failed
    pause
    exit /b 1
)

REM Create MSI installer
echo Creating secure MSI installer...
call gradlew.bat createMsi
if errorlevel 1 (
    echo ERROR: MSI creation failed
    echo.
    echo Possible solutions:
    echo 1. Install WiX Toolset v3.11.2
    echo 2. Run as Administrator
    echo 3. Check if antivirus is blocking the build
    pause
    exit /b 1
)

REM Verify MSI was created
if exist build\msi\SecurePDFEditor-*.msi (
    echo.
    echo ========================================
    echo SUCCESS: Secure MSI installer created!
    echo ========================================
    echo.
    echo Installer location: build\msi\SecurePDFEditor-*.msi
    echo.
    echo Security features included:
    echo - Anti-malware protection
    echo - File integrity verification
    echo - Windows Firewall integration
    echo - Windows Defender exclusions
    echo - Secure installation with admin privileges
    echo - Registry security entries
    echo.
    echo To install:
    echo 1. Right-click the MSI file
    echo 2. Select "Run as administrator"
    echo 3. Follow the installation wizard
    echo.
    echo To verify installation:
    echo - Check logs in C:\Program Files\SecurePDFEditor\logs\
    echo - Verify Windows Firewall rules
    echo - Check Windows Defender exclusions
    echo.
) else (
    echo ERROR: MSI file not found in build\msi\
    pause
    exit /b 1
)

echo Build completed successfully!
pause
