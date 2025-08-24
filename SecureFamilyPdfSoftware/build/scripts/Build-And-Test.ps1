#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Build and test script for Secure Family PDF Software.

.DESCRIPTION
    This script builds the solution in both Debug and Release configurations
    and runs all tests. It supports offline builds after initial restore.

.PARAMETER Configuration
    Build configuration (Debug or Release). Defaults to Release.

.PARAMETER Offline
    Skip package restore and use cached packages only.

.PARAMETER SkipTests
    Skip running tests after build.

.PARAMETER Verbose
    Enable verbose output.

.EXAMPLE
    .\Build-And-Test.ps1
    Builds in Release configuration and runs tests.

.EXAMPLE
    .\Build-And-Test.ps1 -Configuration Debug -Offline
    Builds in Debug configuration using cached packages only.

.EXAMPLE
    .\Build-And-Test.ps1 -SkipTests
    Builds without running tests.
#>

param(
    [ValidateSet("Debug", "Release")]
    [string]$Configuration = "Release",
    
    [switch]$Offline,
    
    [switch]$SkipTests,
    
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
    
    # Check PowerShell version
    $psVersion = $PSVersionTable.PSVersion
    if ($psVersion.Major -lt 5) {
        Write-ColorOutput "✗ PowerShell 5.1 or later required. Current version: $psVersion" "Red"
        exit 1
    }
    Write-ColorOutput "✓ PowerShell version: $psVersion" "Green"
    
    # Check if we're on Windows
    if ($env:OS -ne "Windows_NT") {
        Write-ColorOutput "✗ This script is designed for Windows only." "Red"
        exit 1
    }
    Write-ColorOutput "✓ Running on Windows" "Green"
}

# Function to restore packages
function Restore-Packages {
    if ($Offline) {
        Write-ColorOutput "Skipping package restore (offline mode)" "Yellow"
        return
    }
    
    Write-ColorOutput "Restoring NuGet packages..." "Yellow"
    
    try {
        $restoreArgs = @("restore")
        if ($Verbose) {
            $restoreArgs += "--verbosity", "detailed"
        }
        
        dotnet $restoreArgs
        
        if ($LASTEXITCODE -ne 0) {
            throw "Package restore failed with exit code $LASTEXITCODE"
        }
        
        Write-ColorOutput "✓ Packages restored successfully" "Green"
    }
    catch {
        Write-ColorOutput "✗ Package restore failed: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to build solution
function Invoke-SolutionBuild {
    Write-ColorOutput "Building solution in $Configuration configuration..." "Yellow"
    
    try {
        $buildArgs = @(
            "build",
            "--configuration", $Configuration,
            "--no-restore"
        )
        
        if ($Verbose) {
            $buildArgs += "--verbosity", "detailed"
        }
        
        dotnet $buildArgs
        
        if ($LASTEXITCODE -ne 0) {
            throw "Build failed with exit code $LASTEXITCODE"
        }
        
        Write-ColorOutput "✓ Solution built successfully" "Green"
    }
    catch {
        Write-ColorOutput "✗ Build failed: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to run tests
function Invoke-Tests {
    if ($SkipTests) {
        Write-ColorOutput "Skipping tests as requested" "Yellow"
        return
    }
    
    Write-ColorOutput "Running tests..." "Yellow"
    
    try {
        $testArgs = @(
            "test",
            "--configuration", $Configuration,
            "--no-restore",
            "--no-build"
        )
        
        if ($Verbose) {
            $testArgs += "--verbosity", "detailed"
        }
        
        dotnet $testArgs
        
        if ($LASTEXITCODE -ne 0) {
            throw "Tests failed with exit code $LASTEXITCODE"
        }
        
        Write-ColorOutput "✓ All tests passed" "Green"
    }
    catch {
        Write-ColorOutput "✗ Tests failed: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to create build artifacts
function New-BuildArtifacts {
    Write-ColorOutput "Creating build artifacts..." "Yellow"
    
    try {
        # Create publish directory
        $publishDir = "publish"
        if (Test-Path $publishDir) {
            Remove-Item $publishDir -Recurse -Force
        }
        New-Item -ItemType Directory -Path $publishDir | Out-Null
        
        # Publish main application
        $publishArgs = @(
            "publish",
            "src/SecureFamilyPdf/SecureFamilyPdf.csproj",
            "--configuration", $Configuration,
            "--runtime", "win-x64",
            "--self-contained", "true",
            "--output", $publishDir,
            "--no-restore"
        )
        
        if ($Verbose) {
            $publishArgs += "--verbosity", "detailed"
        }
        
        dotnet $publishArgs
        
        if ($LASTEXITCODE -ne 0) {
            throw "Publish failed with exit code $LASTEXITCODE"
        }
        
        Write-ColorOutput "✓ Build artifacts created in $publishDir" "Green"
    }
    catch {
        Write-ColorOutput "✗ Failed to create build artifacts: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Main execution
try {
    Write-ColorOutput "=== Secure Family PDF Software Build Script ===" "Cyan"
    Write-ColorOutput "Configuration: $Configuration" "White"
    Write-ColorOutput "Offline Mode: $Offline" "White"
    Write-ColorOutput "Skip Tests: $SkipTests" "White"
    Write-ColorOutput "Verbose: $Verbose" "White"
    Write-ColorOutput ""
    
    # Check prerequisites
    Test-Prerequisites
    
    # Restore packages (unless offline)
    Restore-Packages
    
    # Build solution
    Invoke-SolutionBuild
    
    # Run tests (unless skipped)
    Invoke-Tests
    
    # Create build artifacts
    New-BuildArtifacts
    
    Write-ColorOutput ""
    Write-ColorOutput "=== Build completed successfully! ===" "Green"
    Write-ColorOutput "Build artifacts are available in the 'publish' directory." "White"
}
catch {
    Write-ColorOutput ""
    Write-ColorOutput "=== Build failed! ===" "Red"
    Write-ColorOutput "Error: $($_.Exception.Message)" "Red"
    exit 1
}
