# Secure Family PDF Software - Norton Exclusion Script
# Run this script as Administrator to add Norton exclusions

param(
    [switch]$Remove
)

$ErrorActionPreference = "Continue"

function Write-Status {
    param([string]$Message, [string]$Status = "Info")

    $colors = @{
        "Success" = "Green"
        "Error" = "Red"
        "Warning" = "Yellow"
        "Info" = "Cyan"
    }

    $color = $colors[$Status]
    Write-Host $Message -ForegroundColor $color
}

function Add-NortonExclusions {
    $appPath = Split-Path -Parent $MyInvocation.MyCommand.Path
    $exePath = Join-Path $appPath "SecureFamilyPdf.exe"

    Write-Status "Adding Norton Antivirus exclusions..." "Info"
    Write-Status "Application Directory: $appPath" "Info"
    Write-Status "Executable Path: $exePath" "Info"
    Write-Host ""

    # Method 1: Windows Defender exclusions (works for most antivirus)
    try {
        Add-MpPreference -ExclusionPath $appPath -Force -ErrorAction Stop
        Write-Status "✓ Windows Defender exclusion added for directory" "Success"
    }
    catch {
        Write-Status "✗ Windows Defender exclusion failed: $($_.Exception.Message)" "Error"
    }

    try {
        Add-MpPreference -ExclusionPath $exePath -Force -ErrorAction Stop
        Write-Status "✓ Windows Defender exclusion added for executable" "Success"
    }
    catch {
        Write-Status "✗ Windows Defender exclusion failed: $($_.Exception.Message)" "Error"
    }

    # Method 2: Norton-specific registry exclusions
    try {
        $nortonKey = "HKLM:\SOFTWARE\Symantec\Norton AntiVirus\Exclusions"
        if (Test-Path $nortonKey) {
            New-ItemProperty -Path $nortonKey -Name "SecureFamilyPdf" -Value $appPath -PropertyType String -Force
            Write-Status "✓ Norton registry exclusion added" "Success"
        }
        else {
            Write-Status "- Norton registry key not found (may not be Norton)" "Warning"
        }
    }
    catch {
        Write-Status "- Norton registry exclusion failed: $($_.Exception.Message)" "Warning"
    }

    # Method 3: Common antivirus exclusion paths
    $avPaths = @(
        "HKLM:\SOFTWARE\McAfee\AVEngine",
        "HKLM:\SOFTWARE\TrendMicro\PC-cillin",
        "HKLM:\SOFTWARE\KasperskyLab"
    )

    foreach ($avPath in $avPaths) {
        if (Test-Path $avPath) {
            Write-Status "- Found antivirus: $($avPath.Split('\')[-1])" "Info"
        }
    }

    Write-Host ""
    Write-Status "Exclusions added successfully!" "Success"
    Write-Status "You may need to restart your antivirus software for changes to take effect." "Warning"
}

function Remove-NortonExclusions {
    $appPath = Split-Path -Parent $MyInvocation.MyCommand.Path
    $exePath = Join-Path $appPath "SecureFamilyPdf.exe"

    Write-Status "Removing Norton Antivirus exclusions..." "Info"

    # Remove Windows Defender exclusions
    try {
        Remove-MpPreference -ExclusionPath $appPath -Force -ErrorAction Stop
        Write-Status "✓ Windows Defender exclusion removed for directory" "Success"
    }
    catch {
        Write-Status "✗ Windows Defender exclusion removal failed: $($_.Exception.Message)" "Error"
    }

    try {
        Remove-MpPreference -ExclusionPath $exePath -Force -ErrorAction Stop
        Write-Status "✓ Windows Defender exclusion removed for executable" "Success"
    }
    catch {
        Write-Status "✗ Windows Defender exclusion removal failed: $($_.Exception.Message)" "Error"
    }

    # Remove Norton registry exclusions
    try {
        $nortonKey = "HKLM:\SOFTWARE\Symantec\Norton AntiVirus\Exclusions"
        if (Test-Path $nortonKey) {
            Remove-ItemProperty -Path $nortonKey -Name "SecureFamilyPdf" -Force -ErrorAction Stop
            Write-Status "✓ Norton registry exclusion removed" "Success"
        }
    }
    catch {
        Write-Status "- Norton registry exclusion removal failed: $($_.Exception.Message)" "Warning"
    }

    Write-Host ""
    Write-Status "Exclusions removed successfully!" "Success"
}

# Check if running as administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")

if (-not $isAdmin) {
    Write-Status "This script requires administrator privileges!" "Error"
    Write-Status "Please right-click and select 'Run as administrator'" "Warning"
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Status "=== Secure Family PDF Software - Norton Exclusion Manager ===" "Info"
Write-Host ""

if ($Remove) {
    Remove-NortonExclusions
}
else {
    Add-NortonExclusions
}

Write-Host ""
Read-Host "Press Enter to exit"

