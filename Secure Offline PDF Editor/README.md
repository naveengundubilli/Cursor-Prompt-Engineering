# Secure Offline PDF Editor

A secure, offline-first PDF editor built with Java 21, JavaFX, Apache PDFBox, BouncyCastle, and Tess4J.

## 🚀 Features

### Core Features
- **Secure PDF Viewing**: Open and view PDF documents with password protection support
- **Offline-First Design**: Works completely offline with network access controls
- **PDF Editing**: Add text, images, shapes, and annotations to PDFs
- **Digital Signing**: Draw signatures, upload images, or use certificate-based signing
- **Form Handling**: Create and fill PDF forms with validation
- **Search & Navigation**: Text search, bookmarks, and table of contents
- **PDF Assembly**: Merge, split, and insert pages from multiple PDFs
- **OCR Integration**: Extract text from scanned documents using Tesseract
- **Internationalization**: Multi-language support (currently English)

### Security Features
- **AES-256 Encryption**: Secure PDF encryption and decryption
- **Network Guard**: Configurable offline/online mode
- **Secure Temp Files**: Automatic cleanup of temporary files
- **Permission Model**: Respect PDF security permissions
- **Certificate Validation**: Digital signature verification

## 📋 Prerequisites

### Required Software
- **Java 21** (OpenJDK 21 or Oracle JDK 21)
- **Gradle 9.0.0+** (included via wrapper)
- **Windows 10/11** (64-bit)

### Optional Software
- **Tesseract OCR** (for OCR functionality)
- **Git** (for source code management)

## 🛠️ Installation & Setup

### 1. Install Java 21

Download and install Java 21 from:
- **OpenJDK**: https://adoptium.net/temurin/releases/?version=21
- **Oracle JDK**: https://www.oracle.com/java/technologies/downloads/#java21

Verify installation:
```cmd
java -version
javac -version
```

### 2. Clone the Repository

```cmd
git clone https://github.com/your-username/secure-offline-pdf-editor.git
cd secure-offline-pdf-editor
```

### 3. Build the Application

#### Quick Build (Development)
```cmd
.\gradlew.bat build
```

#### Run Tests
```cmd
.\gradlew.bat test
```

#### Run Quality Checks
```cmd
.\gradlew.bat quality
```

### 4. Run the Application

#### Development Mode
```cmd
.\gradlew.bat run
```

#### From JAR File
```cmd
.\gradlew.bat jar
java -jar build/libs/secure-offline-pdf-editor-1.0.0.jar
```

## 📦 Packaging & Distribution

### Create Application Package

The application can be packaged for distribution using the custom packaging task:

```cmd
.\gradlew.bat packageApp
```

This creates a platform-specific package in the `dist/` directory:
- `secure-pdf-editor-1.0.0-windows.zip`

### Package Contents

The package includes:
- Application JAR file
- Gradle wrapper for easy building
- Build configuration files
- Resources and dependencies
- Documentation and license files

### Install on Windows

1. **Extract the package**:
   ```cmd
   # Extract the downloaded zip file
   Expand-Archive secure-pdf-editor-1.0.0-windows.zip -DestinationPath C:\SecurePDFEditor
   ```

2. **Run the application**:
   ```cmd
   cd C:\SecurePDFEditor
   .\gradlew.bat run
   ```

3. **Create desktop shortcut** (optional):
   - Right-click on desktop → New → Shortcut
   - Browse to the extracted folder
   - Select `gradlew.bat` and add `run` as argument
   - Name it "Secure PDF Editor"

## 🔧 Configuration

### Network Mode

The application can run in two modes:

#### Offline Mode (Default)
- No network access allowed
- Maximum security
- All features work offline

#### Online Mode
- Limited network access for updates
- Enable via application settings

### OCR Configuration

For OCR functionality:

1. **Install Tesseract**:
   - Download from: https://github.com/UB-Mannheim/tesseract/wiki
   - Install to default location: `C:\Program Files\Tesseract-OCR`

2. **Configure tessdata**:
   - Create `tessdata` folder in project root
   - Download language files from: https://github.com/tesseract-ocr/tessdata
   - Place `.traineddata` files in `tessdata/` folder

## 🎯 Usage Guide

### Basic Operations

#### Opening PDFs
1. **File → Open** or press `Ctrl+O`
2. Select PDF file (supports password-protected PDFs)
3. Enter password if prompted

#### Saving PDFs
1. **File → Save** or press `Ctrl+S`
2. **File → Save As** or press `Ctrl+Shift+S` for new location

#### PDF/A Export
1. **File → Export → PDF/A**
2. Choose PDF/A-1b compliance level
3. Select output location

### Editing Features

#### Text Editing
1. Select **Edit → Add Text**
2. Click on PDF where you want to add text
3. Type your text
4. Use **Undo** (`Ctrl+Z`) or **Redo** (`Ctrl+Y`) as needed

#### Image Insertion
1. Select **Edit → Insert Image**
2. Choose image file (JPG, PNG)
3. Click on PDF to place image
4. Resize and reposition as needed

#### Drawing Shapes
1. Select **Edit → Draw Shapes**
2. Choose shape type (rectangle, circle, line)
3. Draw on PDF by clicking and dragging

#### Annotations
1. Select annotation type from **Annotations** menu
2. Highlight, underline, or add sticky notes
3. Use **Layers** panel to manage annotation visibility

### Digital Signing

#### Draw Signature
1. **Sign → Draw Signature**
2. Draw your signature on the canvas
3. Click **Apply** to add to PDF

#### Upload Signature
1. **Sign → Upload Signature**
2. Select signature image file
3. Position and resize on PDF

#### Certificate Signing
1. **Sign → Certificate Signing**
2. Select PKCS#12 keystore file
3. Enter keystore password
4. Choose certificate and sign

### Form Handling

#### Create Forms
1. **Forms → Add Text Field**
2. **Forms → Add Checkbox**
3. **Forms → Add Radio Button**
4. Position form fields on PDF

#### Fill Forms
1. Open PDF with form fields
2. Click on fields to edit
3. **Forms → Export Form Data** to save responses

### Search & Navigation

#### Text Search
1. **Search → Find** or press `Ctrl+F`
2. Enter search term
3. Use **Next** and **Previous** to navigate matches

#### Bookmarks
1. **Navigation → Add Bookmark**
2. Navigate to desired page
3. Use **Navigation → Bookmarks** to jump to bookmarks

#### Table of Contents
1. **Navigation → Generate TOC**
2. Automatic TOC generation from PDF structure
3. Use **Navigation → Table of Contents** to navigate

### PDF Assembly

#### Merge PDFs
1. **Tools → Merge PDFs**
2. Select multiple PDF files
3. Choose merge order
4. Save merged PDF

#### Split PDF
1. **Tools → Split PDF**
2. Select page range
3. Save individual pages as separate PDFs

#### Insert Pages
1. **Tools → Insert Pages**
2. Select source PDF
3. Choose insertion point
4. Select pages to insert

### OCR Processing

#### Run OCR
1. **Tools → Run OCR**
2. Wait for processing to complete
3. Text becomes selectable and searchable
4. **Tools → Copy OCR Text** to extract text

## 🧪 Testing

### Run All Tests
```cmd
.\gradlew.bat test
```

### Run Specific Test Categories
```cmd
# Unit tests only
.\gradlew.bat test --tests "*Test"

# Integration tests
.\gradlew.bat test --tests "*IntegrationTest"

# Security tests
.\gradlew.bat test --tests "*SecurityTest"
```

### Quality Checks
```cmd
# Run all quality checks
.\gradlew.bat quality

# Run SpotBugs static analysis
.\gradlew.bat spotbugsMain

# Run OWASP dependency check
.\gradlew.bat dependencyCheckAnalyze
```

## 🔍 Troubleshooting

### Common Issues

#### Java Version Issues
```cmd
# Verify Java version
java -version

# Should show Java 21
# If not, update JAVA_HOME environment variable
```

#### Gradle Issues
```cmd
# Clean and rebuild
.\gradlew.bat clean build

# Run with debug info
.\gradlew.bat build --debug
```

#### Memory Issues
```cmd
# Increase JVM memory
set GRADLE_OPTS=-Xmx4g
.\gradlew.bat build
```

#### PDF Loading Issues
- Ensure PDF is not corrupted
- Check if PDF is password-protected
- Verify PDF version compatibility

#### OCR Issues
- Verify Tesseract installation
- Check tessdata language files
- Ensure sufficient memory for OCR processing

### Log Files

Application logs are stored in:
- `logs/` directory (if configured)
- Console output during development
- System logs for packaged application

### Performance Optimization

#### For Large PDFs
- Increase JVM heap size: `-Xmx4g`
- Use SSD storage for better I/O performance
- Close other applications to free memory

#### For OCR Processing
- Use high-quality scanned documents
- Ensure good lighting in original scans
- Process pages in batches for large documents

## 📚 API Documentation

### Key Services

- **PdfService**: Core PDF operations
- **EditingService**: Content editing features
- **SigningService**: Digital signing functionality
- **FormsService**: Form creation and handling
- **SearchNavigationService**: Search and navigation
- **AssemblyService**: PDF assembly operations
- **OcrService**: OCR text extraction
- **SecurityService**: Encryption and security features

### Architecture

The application follows a modular architecture:
- **UI Layer**: JavaFX components and controllers
- **Service Layer**: Business logic and PDF operations
- **Security Layer**: Encryption, permissions, and network controls
- **Data Layer**: File I/O and persistence

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create feature branch: `git checkout -b feature-name`
3. Make changes and test thoroughly
4. Run quality checks: `.\gradlew.bat quality`
5. Commit changes: `git commit -m "Add feature"`
6. Push to branch: `git push origin feature-name`
7. Create Pull Request

### Code Style
- Follow Java coding conventions
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Write unit tests for new features

### Testing Guidelines
- Write tests for all new functionality
- Maintain test coverage above 80%
- Include positive and negative test cases
- Test edge cases and error conditions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Getting Help
- **Issues**: Create GitHub issue with detailed description
- **Documentation**: Check this README and inline code comments
- **Community**: Join discussions in GitHub Discussions

### Reporting Bugs
When reporting bugs, please include:
- Operating system and version
- Java version
- Steps to reproduce
- Expected vs actual behavior
- Error messages and logs
- Sample PDF file (if applicable)

### Feature Requests
- Use GitHub Issues with "enhancement" label
- Describe the feature and use case
- Consider implementation complexity
- Check existing issues for duplicates

## 🔄 Version History

### v1.0.0 (Current)
- Initial release with all core features
- Complete PDF editing and signing capabilities
- OCR integration and form handling
- Cross-platform packaging support
- Comprehensive security features

## 📞 Contact

- **Project**: https://github.com/your-username/secure-offline-pdf-editor
- **Issues**: https://github.com/your-username/secure-offline-pdf-editor/issues
- **Discussions**: https://github.com/your-username/secure-offline-pdf-editor/discussions

---

**Note**: This application is designed for offline use with security as a primary concern. Always verify the integrity of PDF files and digital signatures before processing sensitive documents.
