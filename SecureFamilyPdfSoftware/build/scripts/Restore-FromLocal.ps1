#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Restore NuGet packages from local cache only.

.DESCRIPTION
    This script restores NuGet packages from the local cache only, failing if
    network access is required. Optionally supports a local NuGet folder feed.

.PARAMETER LocalFeedPath
    Optional path to a local NuGet folder feed.

.PARAMETER Verbose
    Enable verbose output.

.EXAMPLE
    .\Restore-FromLocal.ps1
    Restores packages from local cache only.

.EXAMPLE
    .\Restore-FromLocal.ps1 -LocalFeedPath "C:\LocalNuGet"
    Restores packages from local cache and local folder feed.
#>

param(
    [string]$LocalFeedPath,
    
    [switch]$Verbose
)

# Set error action preference
$ErrorActionPreference = "Stop"

# Function to write colored output
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    
    Write-Host $Message -ForegroundColor $Color
}

# Function to check prerequisites
function Test-Prerequisites {
    Write-ColorOutput "Checking prerequisites..." "Yellow"
    
    # Check .NET SDK
    try {
        $dotnetVersion = dotnet --version
        Write-ColorOutput "✓ .NET SDK found: $dotnetVersion" "Green"
    }
    catch {
        Write-ColorOutput "✗ .NET SDK not found. Please install .NET 8 SDK." "Red"
        exit 1
    }
    
    # Check if we're on Windows
    if ($env:OS -ne "Windows_NT") {
        Write-ColorOutput "✗ This script is designed for Windows only." "Red"
        exit 1
    }
    Write-ColorOutput "✓ Running on Windows" "Green"
}

# Function to check network connectivity
function Test-NetworkConnectivity {
    Write-ColorOutput "Testing network connectivity..." "Yellow"
    
    try {
        # Test connection to nuget.org
        $response = Invoke-WebRequest -Uri "https://api.nuget.org/v3/index.json" -UseBasicParsing -TimeoutSec 5
        Write-ColorOutput "✗ Network connectivity detected. This script requires offline operation." "Red"
        Write-ColorOutput "Please disconnect from the network or use the -LocalFeedPath parameter." "Yellow"
        exit 1
    }
    catch {
        Write-ColorOutput "✓ No network connectivity detected (offline mode)" "Green"
    }
}

# Function to restore packages from local cache
function Restore-FromLocalCache {
    Write-ColorOutput "Restoring packages from local cache..." "Yellow"
    
    try {
        $restoreArgs = @("restore", "--no-http")
        
        if ($LocalFeedPath) {
            if (Test-Path $LocalFeedPath) {
                $restoreArgs += "--source", $LocalFeedPath
                Write-ColorOutput "Using local feed: $LocalFeedPath" "Cyan"
            }
            else {
                Write-ColorOutput "✗ Local feed path not found: $LocalFeedPath" "Red"
                exit 1
            }
        }
        
        if ($Verbose) {
            $restoreArgs += "--verbosity", "detailed"
        }
        
        Write-ColorOutput "Executing: dotnet $($restoreArgs -join ' ')" "Cyan"
        dotnet $restoreArgs
        
        if ($LASTEXITCODE -ne 0) {
            throw "Package restore failed with exit code $LASTEXITCODE"
        }
        
        Write-ColorOutput "✓ Packages restored successfully from local cache" "Green"
    }
    catch {
        Write-ColorOutput "✗ Package restore failed: $($_.Exception.Message)" "Red"
        Write-ColorOutput "This may indicate missing packages in the local cache." "Yellow"
        Write-ColorOutput "Run 'dotnet restore' with network connectivity to populate the cache." "Yellow"
        exit 1
    }
}

# Function to verify package cache
function Test-PackageCache {
    Write-ColorOutput "Verifying package cache..." "Yellow"
    
    try {
        # Get NuGet cache location
        $nugetCachePath = dotnet nuget locals all --list | Select-String "global-packages:" | ForEach-Object { $_.ToString().Split(':')[1].Trim() }
        
        if (-not $nugetCachePath) {
            Write-ColorOutput "✗ Could not determine NuGet cache location" "Red"
            return $false
        }
        
        Write-ColorOutput "NuGet cache location: $nugetCachePath" "Cyan"
        
        if (Test-Path $nugetCachePath) {
            $packageCount = (Get-ChildItem $nugetCachePath -Directory | Measure-Object).Count
            Write-ColorOutput "Found $packageCount packages in cache" "Green"
            return $true
        }
        else {
            Write-ColorOutput "✗ NuGet cache directory not found" "Red"
            return $false
        }
    }
    catch {
        Write-ColorOutput "✗ Failed to verify package cache: $($_.Exception.Message)" "Red"
        return $false
    }
}

# Main execution
try {
    Write-ColorOutput "=== Secure Family PDF Software - Local Package Restore ===" "Cyan"
    Write-ColorOutput "Local Feed Path: $(if ($LocalFeedPath) { $LocalFeedPath } else { 'None' })" "White"
    Write-ColorOutput "Verbose: $Verbose" "White"
    Write-ColorOutput ""
    
    # Check prerequisites
    Test-Prerequisites
    
    # Test network connectivity (should fail in offline mode)
    Test-NetworkConnectivity
    
    # Verify package cache
    if (-not (Test-PackageCache)) {
        Write-ColorOutput "✗ Package cache verification failed" "Red"
        exit 1
    }
    
    # Restore packages from local cache
    Restore-FromLocalCache
    
    Write-ColorOutput ""
    Write-ColorOutput "=== Local package restore completed successfully! ===" "Green"
    Write-ColorOutput "All packages restored from local cache without network access." "White"
}
catch {
    Write-ColorOutput ""
    Write-ColorOutput "=== Local package restore failed! ===" "Red"
    Write-ColorOutput "Error: $($_.Exception.Message)" "Red"
    exit 1
}
