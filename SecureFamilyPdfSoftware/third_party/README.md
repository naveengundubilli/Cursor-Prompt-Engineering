# Third-Party Dependencies

This directory contains third-party dependencies bundled with Secure Family PDF Software.

## Dependencies

### qpdf
- **Version**: Latest stable release
- **License**: Apache License 2.0
- **Purpose**: PDF encryption/decryption operations
- **Source**: https://github.com/qpdf/qpdf
- **Usage**: Password protection and removal via command-line interface

### PdfiumViewer
- **Version**: 2.13.0
- **License**: Apache License 2.0
- **Purpose**: PDF rendering and text extraction
- **Source**: https://github.com/pvginkel/PdfiumViewer
- **Usage**: Core PDF viewing functionality

### QuestPDF
- **Version**: 2023.12.6
- **License**: Apache License 2.0
- **Purpose**: PDF generation and manipulation
- **Source**: https://github.com/QuestPDF/QuestPDF
- **Usage**: Annotation overlays and PDF rebuilding

## Installation

These dependencies are automatically included in the application distribution.
No manual installation is required.

## Security

All third-party dependencies are:
- Open source with permissive licenses
- Regularly updated for security patches
- Scanned for vulnerabilities during build process
- Used in offline-only mode

## Verification

Checksums for all third-party files are generated during the build process
and included in the distribution package as `third_party_checksums.txt`.

## Updates

To update third-party dependencies:
1. Update the package references in the project files
2. Rebuild the solution
3. Run the distribution packaging script
4. Verify checksums are updated

## License Compliance

All third-party dependencies are compatible with the MIT license of this project.
License files for each dependency are included in their respective directories.
