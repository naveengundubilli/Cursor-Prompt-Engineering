#!/usr/bin/env pwsh
<# .SYNOPSIS Create final distribution package for Secure Family PDF Software. #>
param([string]$OutputPath = "dist", [switch]$Verbose)

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Test-Prerequisites {
    Write-ColorOutput "Checking prerequisites..." "Cyan"
    
    # Check if .NET SDK is available
    try {
        $dotnetVersion = dotnet --version
        Write-ColorOutput "✓ .NET SDK found: $dotnetVersion" "Green"
    }
    catch {
        Write-ColorOutput "✗ .NET SDK not found" "Red"
        return $false
    }
    
    return $true
}

function New-DistributionPackage {
    param([string]$SourcePath, [string]$TargetPath)
    
    Write-ColorOutput "Creating distribution package..." "Cyan"
    
    # Create target directory
    if (Test-Path $TargetPath) {
        Remove-Item $TargetPath -Recurse -Force
    }
    New-Item -ItemType Directory -Path $TargetPath -Force | Out-Null
    
    # Copy application files
    Write-ColorOutput "Copying application files..." "Yellow"
    Copy-Item "$SourcePath\*" -Destination $TargetPath -Recurse -Force
    
    # Copy documentation
    Write-ColorOutput "Copying documentation..." "Yellow"
    Copy-Item "README.md" -Destination $TargetPath -Force
    Copy-Item "LICENSE" -Destination $TargetPath -Force -ErrorAction SilentlyContinue
    
    # Copy third-party dependencies
    Write-ColorOutput "Copying third-party dependencies..." "Yellow"
    if (Test-Path "third_party") {
        Copy-Item "third_party" -Destination $TargetPath -Recurse -Force
    }
    
    # Create installation script
    Write-ColorOutput "Creating installation script..." "Yellow"
    $installScript = @'
@echo off
echo Installing Secure Family PDF Software...
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running as administrator
) else (
    echo Please run this script as administrator
    pause
    exit /b 1
)

REM Create installation directory
set INSTALL_DIR=%ProgramFiles%\SecureFamilyPdf
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

REM Copy files
echo Copying files...
xcopy /E /I /Y "SecureFamilyPdf.exe" "%INSTALL_DIR%\"
xcopy /E /I /Y "*.dll" "%INSTALL_DIR%\"
xcopy /E /I /Y "Resources" "%INSTALL_DIR%\"

REM Create desktop shortcut
echo Creating desktop shortcut...
set DESKTOP=%USERPROFILE%\Desktop
set SHORTCUT=%DESKTOP%\Secure Family PDF.lnk

powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%SHORTCUT%'); $Shortcut.TargetPath = '%INSTALL_DIR%\SecureFamilyPdf.exe'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.Save()"

REM Create start menu shortcut
echo Creating start menu shortcut...
set START_MENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs
if not exist "%START_MENU%\Secure Family PDF" mkdir "%START_MENU%\Secure Family PDF"

set START_SHORTCUT=%START_MENU%\Secure Family PDF\Secure Family PDF.lnk
powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%START_SHORTCUT%'); $Shortcut.TargetPath = '%INSTALL_DIR%\SecureFamilyPdf.exe'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.Save()"

echo.
echo Installation completed successfully!
echo Secure Family PDF Software has been installed to: %INSTALL_DIR%
echo.
pause
'@
    
    $installScript | Out-File -FilePath "$TargetPath\install.bat" -Encoding ASCII
    
    # Create uninstall script
    $uninstallScript = @'
@echo off
echo Uninstalling Secure Family PDF Software...
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running as administrator
) else (
    echo Please run this script as administrator
    pause
    exit /b 1
)

REM Remove installation directory
set INSTALL_DIR=%ProgramFiles%\SecureFamilyPdf
if exist "%INSTALL_DIR%" (
    echo Removing installation files...
    rmdir /S /Q "%INSTALL_DIR%"
)

REM Remove desktop shortcut
echo Removing desktop shortcut...
set DESKTOP=%USERPROFILE%\Desktop
set SHORTCUT=%DESKTOP%\Secure Family PDF.lnk
if exist "%SHORTCUT%" del "%SHORTCUT%"

REM Remove start menu shortcut
echo Removing start menu shortcut...
set START_MENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs
set START_FOLDER=%START_MENU%\Secure Family PDF
if exist "%START_FOLDER%" rmdir /S /Q "%START_FOLDER%"

echo.
echo Uninstallation completed successfully!
echo.
pause
'@
    
    $uninstallScript | Out-File -FilePath "$TargetPath\uninstall.bat" -Encoding ASCII
    
    # Create README for distribution
    $distReadme = @'
# Secure Family PDF Software - Distribution Package

This package contains the Secure Family PDF Software, a secure, offline PDF viewer and editor for Windows.

## Contents

- `SecureFamilyPdf.exe` - Main application executable
- `*.dll` - Required runtime libraries
- `Resources/` - Application resources
- `install.bat` - Installation script (run as administrator)
- `uninstall.bat` - Uninstallation script (run as administrator)
- `third_party/` - Third-party dependencies and licenses
- `README.md` - Main documentation
- `LICENSE` - Software license

## Installation

1. Extract this package to a temporary location
2. Right-click on `install.bat` and select "Run as administrator"
3. Follow the installation prompts
4. The application will be installed to `%ProgramFiles%\SecureFamilyPdf`
5. Desktop and Start Menu shortcuts will be created automatically

## Uninstallation

1. Right-click on `uninstall.bat` and select "Run as administrator"
2. Follow the uninstallation prompts
3. All files and shortcuts will be removed

## System Requirements

- Windows 10/11 (64-bit)
- No internet connection required (offline application)
- Minimum 100MB free disk space

## Security Features

- Offline by design - no network calls
- PDF JavaScript disabled
- External links disabled
- Embedded files disabled
- Form submission disabled
- True redaction support
- Password protection support

## Usage

1. Launch the application from the Start Menu or Desktop shortcut
2. Use File > Open to load a PDF document
3. Navigate through pages using the toolbar buttons
4. Use zoom controls to adjust the view
5. Apply security features as needed

## Support

For support and documentation, please refer to the main README.md file.

## License

This software is licensed under the MIT License. See LICENSE file for details.
'@
    
    $distReadme | Out-File -FilePath "$TargetPath\DISTRIBUTION_README.md" -Encoding UTF8
    
    Write-ColorOutput "✓ Distribution package created successfully!" "Green"
}

function Get-PackageSize {
    param([string]$Path)
    
    $size = (Get-ChildItem -Path $Path -Recurse | Measure-Object -Property Length -Sum).Sum
    $sizeMB = [math]::Round($size / 1MB, 2)
    return $sizeMB
}

# Main execution
try {
    Write-ColorOutput "=== Secure Family PDF Software - Distribution Creation ===" "Cyan"
    Write-ColorOutput "Output Path: $OutputPath" "White"
    Write-ColorOutput "Verbose: $Verbose" "White"
    
    # Check prerequisites
    if (-not (Test-Prerequisites)) {
        Write-ColorOutput "Prerequisites check failed. Exiting." "Red"
        exit 1
    }
    
    # Check if source distribution exists
    $sourcePath = "dist\SecureFamilyPdf"
    if (-not (Test-Path $sourcePath)) {
        Write-ColorOutput "Source distribution not found at: $sourcePath" "Red"
        Write-ColorOutput "Please run the publish command first: dotnet publish src/SecureFamilyPdf --configuration Release --output dist/SecureFamilyPdf --self-contained --runtime win-x64" "Yellow"
        exit 1
    }
    
    # Create final distribution package
    $finalDistPath = "$OutputPath\SecureFamilyPdf-Final"
    New-DistributionPackage -SourcePath $sourcePath -TargetPath $finalDistPath
    
    # Calculate and display package size
    $packageSize = Get-PackageSize -Path $finalDistPath
    Write-ColorOutput "Package size: $packageSize MB" "Green"
    
    # Create ZIP archive
    Write-ColorOutput "Creating ZIP archive..." "Cyan"
    $zipPath = "$OutputPath\SecureFamilyPdf-Windows-x64.zip"
    if (Test-Path $zipPath) {
        Remove-Item $zipPath -Force
    }
    
    Compress-Archive -Path "$finalDistPath\*" -DestinationPath $zipPath
    $zipSize = Get-PackageSize -Path $zipPath
    Write-ColorOutput "ZIP archive created: $zipPath ($zipSize MB)" "Green"
    
    Write-ColorOutput "=== Distribution Creation Completed ===" "Cyan"
    Write-ColorOutput "Final package location: $finalDistPath" "White"
    Write-ColorOutput "ZIP archive: $zipPath" "White"
    Write-ColorOutput "Ready for testing and distribution!" "Green"
    
} catch {
    Write-ColorOutput "Error: $($_.Exception.Message)" "Red"
    if ($Verbose) {
        Write-ColorOutput "Stack trace: $($_.ScriptStackTrace)" "Red"
    }
    exit 1
}
