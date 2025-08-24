Secure Family PDF Software - README
=====================================

This is a secure, offline PDF viewer and editor for Windows.

QUICK START:
===========

1. EASY WAY (Recommended):
   - Double-click "Run-SecureFamilyPdf.bat"
   - This will automatically add Norton exclusions and launch the app

2. MANUAL WAY:
   - Right-click "Add-NortonExclusions.ps1" and select "Run as administrator"
   - Then double-click "SecureFamilyPdf.exe"

3. DIRECT LAUNCH:
   - Double-click "SecureFamilyPdf.exe" directly
   - If Norton blocks it, use method 1 or 2 above

NORTON ANTIVIRUS EXCLUSIONS:
===========================

The application includes scripts to prevent Norton from scanning:

- "Run-SecureFamilyPdf.bat" - Automatic launcher with exclusions
- "Add-NortonExclusions.ps1" - Manual exclusion manager

To remove exclusions later:
- Right-click "Add-NortonExclusions.ps1" and select "Run as administrator"
- Add the parameter: -Remove

TROUBLESHOOTING:
===============

If the application doesn't start:

1. Try running as administrator
2. Temporarily disable Norton/antivirus
3. Add the folder to Norton exclusions manually
4. Check Windows Event Viewer for error details

SECURITY FEATURES:
=================

- Offline by design (no network calls)
- PDF JavaScript disabled
- External links disabled
- Embedded files disabled
- Form submission disabled
- True redaction support
- Password protection support

SYSTEM REQUIREMENTS:
===================

- Windows 10/11 (64-bit)
- No internet connection required
- Minimum 100MB free disk space

FILES INCLUDED:
==============

- SecureFamilyPdf.exe - Main application
- *.dll - Required runtime libraries
- Run-SecureFamilyPdf.bat - Launcher with Norton exclusions
- Add-NortonExclusions.ps1 - Norton exclusion manager
- README.txt - This file

For more information, see the main README.md file.

