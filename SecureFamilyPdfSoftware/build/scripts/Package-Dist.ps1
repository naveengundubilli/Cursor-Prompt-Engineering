#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Package distribution files for Secure Family PDF Software.

.DESCRIPTION
    This script copies signed binaries to /dist, adds README.md and LICENSE,
    and includes third_party/ with checksums.

.PARAMETER Configuration
    Build configuration (Debug or Release). Defaults to Release.

.PARAMETER OutputPath
    Output directory for the distribution package. Defaults to "dist".

.PARAMETER IncludeSymbols
    Include debug symbols in the distribution.

.PARAMETER Verbose
    Enable verbose output.

.EXAMPLE
    .\Package-Dist.ps1
    Creates distribution package in Release configuration.

.EXAMPLE
    .\Package-Dist.ps1 -Configuration Debug -OutputPath "C:\MyDist"
    Creates distribution package in Debug configuration to specified path.
#>

param(
    [ValidateSet("Debug", "Release")]
    [string]$Configuration = "Release",
    
    [string]$OutputPath = "dist",
    
    [switch]$IncludeSymbols,
    
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

# Function to build the solution
function Build-Solution {
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

# Function to publish the application
function Publish-Application {
    Write-ColorOutput "Publishing application..." "Yellow"
    
    try {
        $publishArgs = @(
            "publish",
            "src/SecureFamilyPdf/SecureFamilyPdf.csproj",
            "--configuration", $Configuration,
            "--runtime", "win-x64",
            "--self-contained", "true",
            "--publish-single-file", "true",
            "--publish-trimmed", "true",
            "--enable-compression-in-single-file", "true",
            "--output", "publish"
        )
        
        if (-not $IncludeSymbols) {
            $publishArgs += "--debug-type", "none"
        }
        
        if ($Verbose) {
            $publishArgs += "--verbosity", "detailed"
        }
        
        dotnet $publishArgs
        
        if ($LASTEXITCODE -ne 0) {
            throw "Publish failed with exit code $LASTEXITCODE"
        }
        
        Write-ColorOutput "✓ Application published successfully" "Green"
    }
    catch {
        Write-ColorOutput "✗ Publish failed: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to create distribution directory
function New-DistributionDirectory {
    Write-ColorOutput "Creating distribution directory..." "Yellow"
    
    try {
        # Remove existing distribution directory
        if (Test-Path $OutputPath) {
            Remove-Item $OutputPath -Recurse -Force
            Write-ColorOutput "Removed existing distribution directory" "Cyan"
        }
        
        # Create new distribution directory
        New-Item -ItemType Directory -Path $OutputPath | Out-Null
        Write-ColorOutput "Created distribution directory: $OutputPath" "Green"
    }
    catch {
        Write-ColorOutput "✗ Failed to create distribution directory: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to copy application files
function Copy-ApplicationFiles {
    Write-ColorOutput "Copying application files..." "Yellow"
    
    try {
        $publishDir = "publish"
        if (-not (Test-Path $publishDir)) {
            throw "Publish directory not found: $publishDir"
        }
        
        # Copy all files from publish directory
        Copy-Item -Path "$publishDir\*" -Destination $OutputPath -Recurse -Force
        
        Write-ColorOutput "✓ Application files copied successfully" "Green"
    }
    catch {
        Write-ColorOutput "✗ Failed to copy application files: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to copy documentation files
function Copy-DocumentationFiles {
    Write-ColorOutput "Copying documentation files..." "Yellow"
    
    try {
        # Copy README.md
        if (Test-Path "README.md") {
            Copy-Item -Path "README.md" -Destination $OutputPath -Force
            Write-ColorOutput "✓ README.md copied" "Green"
        }
        else {
            Write-ColorOutput "⚠ README.md not found" "Yellow"
        }
        
        # Copy LICENSE file
        $licenseFiles = @("LICENSE", "LICENSE.txt", "LICENSE.md")
        $licenseCopied = $false
        
        foreach ($licenseFile in $licenseFiles) {
            if (Test-Path $licenseFile) {
                Copy-Item -Path $licenseFile -Destination $OutputPath -Force
                Write-ColorOutput "✓ $licenseFile copied" "Green"
                $licenseCopied = $true
                break
            }
        }
        
        if (-not $licenseCopied) {
            Write-ColorOutput "⚠ No LICENSE file found" "Yellow"
        }
        
        Write-ColorOutput "✓ Documentation files copied successfully" "Green"
    }
    catch {
        Write-ColorOutput "✗ Failed to copy documentation files: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to copy third-party dependencies
function Copy-ThirdPartyDependencies {
    Write-ColorOutput "Copying third-party dependencies..." "Yellow"
    
    try {
        $thirdPartyDir = "third_party"
        if (Test-Path $thirdPartyDir) {
            $thirdPartyDest = Join-Path $OutputPath "third_party"
            Copy-Item -Path $thirdPartyDir -Destination $thirdPartyDest -Recurse -Force
            Write-ColorOutput "✓ Third-party dependencies copied" "Green"
            
            # Generate checksums for third-party files
            Generate-ThirdPartyChecksums $thirdPartyDest
        }
        else {
            Write-ColorOutput "⚠ Third-party directory not found: $thirdPartyDir" "Yellow"
        }
    }
    catch {
        Write-ColorOutput "✗ Failed to copy third-party dependencies: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to generate checksums for third-party files
function Generate-ThirdPartyChecksums {
    param([string]$ThirdPartyPath)
    
    Write-ColorOutput "Generating checksums for third-party files..." "Yellow"
    
    try {
        $checksumsFile = Join-Path $OutputPath "third_party_checksums.txt"
        $checksums = @()
        
        # Get all files in third-party directory
        $files = Get-ChildItem -Path $ThirdPartyPath -Recurse -File
        
        foreach ($file in $files) {
            $hash = Get-FileHash -Path $file.FullName -Algorithm SHA256
            $relativePath = $file.FullName.Substring($ThirdPartyPath.Length + 1)
            $checksums += "$($hash.Hash)  $relativePath"
        }
        
        # Write checksums to file
        $checksums | Out-File -FilePath $checksumsFile -Encoding UTF8
        
        Write-ColorOutput "✓ Checksums generated: $checksumsFile" "Green"
        Write-ColorOutput "Generated checksums for $($files.Count) files" "Cyan"
    }
    catch {
        Write-ColorOutput "✗ Failed to generate checksums: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to create distribution manifest
function New-DistributionManifest {
    Write-ColorOutput "Creating distribution manifest..." "Yellow"
    
    try {
        $manifestFile = Join-Path $OutputPath "distribution_manifest.txt"
        $manifest = @()
        
        # Add header
        $manifest += "Secure Family PDF Software Distribution Manifest"
        $manifest += "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
        $manifest += "Configuration: $Configuration"
        $manifest += "Include Symbols: $IncludeSymbols"
        $manifest += ""
        
        # Add file listing
        $manifest += "Files:"
        $files = Get-ChildItem -Path $OutputPath -Recurse -File | Sort-Object FullName
        
        foreach ($file in $files) {
            $relativePath = $file.FullName.Substring($OutputPath.Length + 1)
            $size = $file.Length
            $hash = Get-FileHash -Path $file.FullName -Algorithm SHA256
            $manifest += "$relativePath ($size bytes, SHA256: $($hash.Hash))"
        }
        
        # Write manifest to file
        $manifest | Out-File -FilePath $manifestFile -Encoding UTF8
        
        Write-ColorOutput "✓ Distribution manifest created: $manifestFile" "Green"
    }
    catch {
        Write-ColorOutput "✗ Failed to create distribution manifest: $($_.Exception.Message)" "Red"
        exit 1
    }
}

# Function to calculate distribution size
function Get-DistributionSize {
    Write-ColorOutput "Calculating distribution size..." "Yellow"
    
    try {
        $files = Get-ChildItem -Path $OutputPath -Recurse -File
        $totalSize = ($files | Measure-Object -Property Length -Sum).Sum
        
        $sizeMB = [math]::Round($totalSize / 1MB, 2)
        $fileCount = $files.Count
        
        Write-ColorOutput "✓ Distribution size: $sizeMB MB ($fileCount files)" "Green"
        
        return @{
            SizeBytes = $totalSize
            SizeMB = $sizeMB
            FileCount = $fileCount
        }
    }
    catch {
        Write-ColorOutput "✗ Failed to calculate distribution size: $($_.Exception.Message)" "Red"
        return $null
    }
}

# Main execution
try {
    Write-ColorOutput "=== Secure Family PDF Software - Distribution Packaging ===" "Cyan"
    Write-ColorOutput "Configuration: $Configuration" "White"
    Write-ColorOutput "Output Path: $OutputPath" "White"
    Write-ColorOutput "Include Symbols: $IncludeSymbols" "White"
    Write-ColorOutput "Verbose: $Verbose" "White"
    Write-ColorOutput ""
    
    # Check prerequisites
    Test-Prerequisites
    
    # Build the solution
    Build-Solution
    
    # Publish the application
    Publish-Application
    
    # Create distribution directory
    New-DistributionDirectory
    
    # Copy application files
    Copy-ApplicationFiles
    
    # Copy documentation files
    Copy-DocumentationFiles
    
    # Copy third-party dependencies
    Copy-ThirdPartyDependencies
    
    # Create distribution manifest
    New-DistributionManifest
    
    # Calculate and display distribution size
    $sizeInfo = Get-DistributionSize
    
    Write-ColorOutput ""
    Write-ColorOutput "=== Distribution packaging completed successfully! ===" "Green"
    Write-ColorOutput "Distribution location: $((Get-Item $OutputPath).FullName)" "White"
    if ($sizeInfo) {
        Write-ColorOutput "Total size: $($sizeInfo.SizeMB) MB ($($sizeInfo.FileCount) files)" "White"
    }
    Write-ColorOutput ""
    Write-ColorOutput "Distribution package is ready for deployment." "White"
}
catch {
    Write-ColorOutput ""
    Write-ColorOutput "=== Distribution packaging failed! ===" "Red"
    Write-ColorOutput "Error: $($_.Exception.Message)" "Red"
    exit 1
}
