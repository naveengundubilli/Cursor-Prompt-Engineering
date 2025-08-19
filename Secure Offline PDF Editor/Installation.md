# Secure PDF Editor - Installation Guide

This guide provides detailed instructions for installing the Secure PDF Editor with comprehensive security features to prevent malware and trojans.

## 📋 Table of Contents

- [System Requirements](#system-requirements)
- [Prerequisites](#prerequisites)
- [Installation Methods](#installation-methods)
- [MSI Installer Installation](#msi-installer-installation)
- [Security Features](#security-features)
- [Post-Installation Verification](#post-installation-verification)
- [Troubleshooting](#troubleshooting)
- [Uninstallation](#uninstallation)

## 🖥️ System Requirements

### Minimum Requirements
- **Operating System**: Windows 10 (version 1903) or Windows 11
- **Architecture**: 64-bit (x64)
- **Processor**: Intel Core i3 or AMD equivalent (1.5 GHz or faster)
- **Memory**: 4 GB RAM
- **Storage**: 500 MB available disk space
- **Java Runtime**: Java 21 (OpenJDK 21 or Oracle JDK 21)

### Recommended Requirements
- **Operating System**: Windows 11 (latest version)
- **Processor**: Intel Core i5 or AMD equivalent (2.0 GHz or faster)
- **Memory**: 8 GB RAM or more
- **Storage**: 1 GB available disk space (SSD recommended)
- **Security**: Windows Defender enabled and updated

### Security Requirements
- **User Account Control (UAC)**: Enabled
- **Windows Defender**: Enabled and updated
- **Windows Firewall**: Enabled
- **Administrator Rights**: Required for installation

## 🔧 Prerequisites

### 1. Install Java 21

The Secure PDF Editor requires Java 21 for optimal security and performance.

#### Download Java 21
- **OpenJDK 21**: [Download from Adoptium](https://adoptium.net/temurin/releases/?version=21)
- **Oracle JDK 21**: [Download from Oracle](https://www.oracle.com/java/technologies/downloads/#java21)

#### Installation Steps
1. Download the appropriate installer for your system (64-bit Windows)
2. Run the installer as Administrator
3. Follow the installation wizard
4. Verify installation:
   ```cmd
   java -version
   javac -version
   ```

#### Verify Java Installation
```cmd
# Check Java version
java -version

# Expected output:
# openjdk version "21.0.x" 2023-xx-xx
# OpenJDK Runtime Environment (build 21.0.x+xx-xx)
# OpenJDK 64-Bit Server VM (build 21.0.x+xx-xx, mixed mode, sharing)
```

### 2. Enable Windows Security Features

#### Windows Defender
1. Open Windows Security (Windows Defender Security Center)
2. Ensure "Real-time protection" is enabled
3. Update virus definitions
4. Run a quick scan to ensure system is clean

#### Windows Firewall
1. Open Windows Defender Firewall
2. Ensure firewall is enabled for all network profiles
3. Allow Windows Defender Firewall to manage connections

#### User Account Control (UAC)
1. Open Control Panel → User Accounts → User Accounts
2. Click "Change User Account Control settings"
3. Ensure UAC is set to "Notify me only when apps try to make changes to my computer"

## 📦 Installation Methods

### Method 1: MSI Installer (Recommended)

The MSI installer provides the most secure installation method with comprehensive security features.

### Method 2: Manual Installation

For advanced users who prefer manual installation or have specific requirements.

## 🔐 MSI Installer Installation

### Step 1: Download the Installer

1. Download `SecurePDFEditor-1.0.0.msi` from the official distribution
2. Verify the file integrity using the provided checksums
3. Ensure the file is from a trusted source

### Step 2: Prepare for Installation

#### Verify System Readiness
```cmd
# Check Windows version
ver

# Verify administrator privileges
whoami /groups | findstr "Administrators"

# Check Java installation
java -version

# Verify Windows Defender status
Get-MpComputerStatus | Select-Object AntivirusEnabled, RealTimeProtectionEnabled
```

#### Disable Conflicting Software
- Temporarily disable third-party antivirus software
- Close any running PDF editors
- Ensure no other installations are in progress

### Step 3: Install the Application

#### GUI Installation
1. **Right-click** the MSI file
2. Select **"Run as administrator"**
3. Click **"Yes"** when prompted by UAC
4. Follow the installation wizard:
   - Accept the license agreement
   - Choose installation directory (default: `C:\Program Files\SecurePDFEditor`)
   - Select components to install
   - Choose Start Menu and Desktop shortcuts
5. Click **"Install"** to begin installation

#### Command Line Installation
```cmd
# Silent installation (recommended for enterprise deployment)
msiexec /i SecurePDFEditor-1.0.0.msi /quiet /l*v install.log

# Interactive installation with logging
msiexec /i SecurePDFEditor-1.0.0.msi /l*v install.log

# Installation with custom properties
msiexec /i SecurePDFEditor-1.0.0.msi INSTALLDIR="D:\SecurePDFEditor" /quiet
```

### Step 4: Installation Verification

The installer automatically performs several verification steps:

#### File Integrity Check
- Verifies all installed files using SHA-256 hashes
- Ensures no files were corrupted during installation
- Validates digital signatures (if present)

#### Security Configuration
- Creates secure registry entries
- Configures Windows Firewall rules
- Sets up Windows Defender exclusions
- Establishes file permissions

#### Installation Logs
Check the installation logs for verification:
```cmd
# View installation log
type install.log | findstr "SUCCESS\|ERROR\|WARNING"

# Check for security-related entries
type install.log | findstr "Security\|Firewall\|Defender"
```

## 🛡️ Security Features

### Anti-Malware Protection

#### Real-time Scanning
- **Continuous monitoring** of application files
- **Heuristic analysis** for unknown threats
- **Signature scanning** for known malware
- **Entropy analysis** for packed/obfuscated code

#### File Quarantine
- **Automatic isolation** of suspicious files
- **Secure quarantine** directory with restricted access
- **Threat logging** and alerting system

### File Integrity Verification

#### SHA-256 Hashing
- **Cryptographic verification** of all application files
- **Continuous monitoring** for unauthorized modifications
- **Tamper detection** and alerting

#### Integrity Monitoring
- **Real-time file monitoring** during application execution
- **Automatic rollback** of compromised files
- **Security event logging** for audit trails

### Installation Security

#### Elevated Privileges
- **Administrator rights** required for installation
- **Secure installation** to protected system directories
- **UAC compliance** for user security

#### Permission Controls
- **Restricted file permissions** for sensitive directories
- **User access controls** for application files
- **Audit logging** for security events

### Network Security

#### Firewall Integration
- **Automatic Windows Firewall** rule creation
- **Inbound traffic blocking** for security
- **Connection monitoring** and logging

#### Windows Defender Integration
- **Automatic exclusion** configuration
- **Real-time protection** compatibility
- **Threat detection** coordination

## ✅ Post-Installation Verification

### Step 1: Verify Installation

#### Check Installation Directory
```cmd
# Verify files are installed correctly
dir "C:\Program Files\SecurePDFEditor"

# Expected files:
# - SecurePDFEditor.jar (main application)
# - config.properties (security configuration)
# - logs\ (security logs directory)
# - temp\ (temporary files directory)
# - quarantine\ (quarantine directory)
```

#### Verify File Permissions
```cmd
# Check file permissions
icacls "C:\Program Files\SecurePDFEditor"

# Expected permissions:
# - Administrators: Full control
# - Users: Read and execute
# - System: Full control
```

### Step 2: Verify Security Features

#### Check Windows Firewall Rules
```cmd
# Verify firewall rules were created
netsh advfirewall firewall show rule name="Secure PDF Editor"

# Expected output should show:
# - Rule Name: Secure PDF Editor
# - Enabled: Yes
# - Direction: In
# - Action: Block
```

#### Check Windows Defender Exclusions
```cmd
# Verify Windows Defender exclusions
reg query "HKLM\SOFTWARE\Microsoft\Windows Defender\Exclusions\Paths" /s

# Should show entries for:
# - C:\Program Files\SecurePDFEditor
# - C:\Program Files\SecurePDFEditor\temp
# - C:\Program Files\SecurePDFEditor\logs
```

#### Verify Registry Security Entries
```cmd
# Check security registry entries
reg query "HKLM\SOFTWARE\SecurePDFEditor\Security" /s

# Should show:
# - InstallPath
# - Version
# - SecureMode
# - AntiTamper
# - FileIntegrity
# - InstallDate
# - InstallUser
```

### Step 3: Test Application Launch

#### Launch from Start Menu
1. Open Start Menu
2. Search for "Secure PDF Editor"
3. Click the application icon
4. Verify the application starts without errors

#### Launch from Desktop Shortcut
1. Double-click the desktop shortcut (if created)
2. Verify the application launches correctly
3. Check for any security warnings or prompts

#### Launch from Command Line
```cmd
# Launch application from command line
cd "C:\Program Files\SecurePDFEditor"
java -jar SecurePDFEditor.jar

# Check for any error messages or security alerts
```

### Step 4: Verify Security Logging

#### Check Security Logs
```cmd
# View security log file
type "C:\Program Files\SecurePDFEditor\logs\security.log"

# Look for entries like:
# - SECURITY_INITIALIZED
# - FILE_REGISTERED
# - BEHAVIOR_ANALYSIS
# - HEURISTIC_SCAN
```

#### Monitor Real-time Security Events
```cmd
# Monitor security log in real-time
powershell "Get-Content 'C:\Program Files\SecurePDFEditor\logs\security.log' -Wait -Tail 10"
```

## 🔧 Troubleshooting

### Common Installation Issues

#### "Access Denied" Errors
**Problem**: Installation fails with access denied errors
**Solution**:
```cmd
# Ensure you're running as administrator
whoami /groups | findstr "Administrators"

# Check file permissions
icacls "C:\Program Files"

# Temporarily disable antivirus
# Re-run installation as administrator
```

#### Java Not Found
**Problem**: Application fails to start due to missing Java
**Solution**:
```cmd
# Verify Java installation
java -version

# Check JAVA_HOME environment variable
echo %JAVA_HOME%

# Set JAVA_HOME if needed
setx JAVA_HOME "C:\Program Files\Java\jdk-21" /M
```

#### Windows Defender Blocking Installation
**Problem**: Windows Defender blocks the installation
**Solution**:
1. Open Windows Security
2. Go to "Virus & threat protection"
3. Click "Manage settings"
4. Add exclusion for installation directory
5. Re-run installation

#### Firewall Rules Not Created
**Problem**: Windows Firewall rules are missing
**Solution**:
```cmd
# Manually create firewall rules
netsh advfirewall firewall add rule name="Secure PDF Editor" dir=in action=block program="C:\Program Files\SecurePDFEditor\SecurePDFEditor.jar"

# Verify rules were created
netsh advfirewall firewall show rule name="Secure PDF Editor"
```

### Security Feature Issues

#### File Integrity Violations
**Problem**: Application reports file integrity violations
**Solution**:
```cmd
# Check for file modifications
dir "C:\Program Files\SecurePDFEditor" /T:W

# Verify file hashes
certutil -hashfile "C:\Program Files\SecurePDFEditor\SecurePDFEditor.jar" SHA256

# Reinstall if necessary
msiexec /x SecurePDFEditor-1.0.0.msi /quiet
msiexec /i SecurePDFEditor-1.0.0.msi /quiet
```

#### Malware Detection False Positives
**Problem**: Application flags legitimate files as suspicious
**Solution**:
1. Check the security log for details
2. Verify the flagged file is legitimate
3. Add file to exclusion list if needed
4. Update security signatures

#### Performance Issues
**Problem**: Application runs slowly due to security scanning
**Solution**:
```cmd
# Check system resources
tasklist | findstr java
wmic cpu get loadpercentage

# Adjust security settings if needed
# Consider reducing scan frequency for better performance
```

### Log Analysis

#### Security Log Analysis
```cmd
# View recent security events
powershell "Get-Content 'C:\Program Files\SecurePDFEditor\logs\security.log' | Select-Object -Last 20"

# Search for specific events
findstr "ERROR\|WARNING\|THREAT" "C:\Program Files\SecurePDFEditor\logs\security.log"

# Export log for analysis
copy "C:\Program Files\SecurePDFEditor\logs\security.log" "%USERPROFILE%\Desktop\security_analysis.log"
```

#### Installation Log Analysis
```cmd
# View installation log
type install.log | findstr "SUCCESS\|ERROR\|WARNING"

# Search for specific installation issues
findstr "Security\|Firewall\|Defender\|Permission" install.log
```

## 🗑️ Uninstallation

### Method 1: Using Control Panel

1. Open **Control Panel** → **Programs and Features**
2. Find **"Secure PDF Editor"** in the list
3. Click **"Uninstall"**
4. Follow the uninstallation wizard
5. Restart the computer if prompted

### Method 2: Using Command Line

```cmd
# Silent uninstallation
msiexec /x SecurePDFEditor-1.0.0.msi /quiet

# Uninstallation with logging
msiexec /x SecurePDFEditor-1.0.0.msi /l*v uninstall.log

# Force uninstallation (if normal uninstall fails)
msiexec /x {PRODUCT-GUID} /quiet /force
```

### Method 3: Manual Cleanup

If automatic uninstallation fails:

#### Remove Application Files
```cmd
# Remove application directory
rmdir /s /q "C:\Program Files\SecurePDFEditor"

# Remove user data (if any)
rmdir /s /q "%APPDATA%\SecurePDFEditor"
rmdir /s /q "%LOCALAPPDATA%\SecurePDFEditor"
```

#### Remove Registry Entries
```cmd
# Remove application registry keys
reg delete "HKLM\SOFTWARE\SecurePDFEditor" /f
reg delete "HKCU\SOFTWARE\SecurePDFEditor" /f

# Remove Windows Defender exclusions
reg delete "HKLM\SOFTWARE\Microsoft\Windows Defender\Exclusions\Paths" /v "C:\Program Files\SecurePDFEditor" /f
```

#### Remove Firewall Rules
```cmd
# Remove firewall rules
netsh advfirewall firewall delete rule name="Secure PDF Editor"
```

#### Remove Start Menu Shortcuts
```cmd
# Remove Start Menu shortcuts
rmdir /s /q "%ProgramData%\Microsoft\Windows\Start Menu\Programs\Secure PDF Editor"

# Remove desktop shortcut
del "%USERPROFILE%\Desktop\Secure PDF Editor.lnk"
```

### Post-Uninstallation Verification

#### Verify Complete Removal
```cmd
# Check if application directory exists
dir "C:\Program Files\SecurePDFEditor" 2>nul || echo "Application directory removed"

# Check if registry keys exist
reg query "HKLM\SOFTWARE\SecurePDFEditor" 2>nul || echo "Registry keys removed"

# Check if firewall rules exist
netsh advfirewall firewall show rule name="Secure PDF Editor" 2>nul || echo "Firewall rules removed"
```

#### Clean Up Temporary Files
```cmd
# Clean temporary files
del /q /f "%TEMP%\SecurePDFEditor*" 2>nul
del /q /f "%TEMP%\*.msi" 2>nul

# Clean Windows temporary files
del /q /f "%WINDIR%\Temp\SecurePDFEditor*" 2>nul
```

## 📞 Support

### Getting Help

If you encounter issues during installation:

1. **Check the logs**: Review installation and security logs for error details
2. **Verify prerequisites**: Ensure all system requirements are met
3. **Contact support**: Create an issue on the project's GitHub page
4. **Community help**: Check existing issues and discussions

### Useful Commands

#### System Information
```cmd
# Get system information
systeminfo | findstr /B /C:"OS Name" /C:"OS Version" /C:"System Type"

# Get Java information
java -version
javac -version

# Get security status
Get-MpComputerStatus | Select-Object AntivirusEnabled, RealTimeProtectionEnabled, QuickScanSignatureVersion
```

#### Security Verification
```cmd
# Verify Windows Defender status
Get-MpThreatDetection

# Check firewall status
netsh advfirewall show allprofiles

# Verify UAC settings
reg query "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Policies\System" /v EnableLUA
```

---

**Note**: This installation guide is designed for the secure MSI installer version of the Secure PDF Editor. Always verify the integrity of downloaded files and ensure you're installing from a trusted source. The application is designed for offline use with security as a primary concern.
