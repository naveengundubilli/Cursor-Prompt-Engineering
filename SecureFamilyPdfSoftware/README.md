# Secure PDF Editor

A **security-first**, **offline-only** PDF editor built with .NET 8 WPF, designed for users who prioritize data privacy and security.

## 🛡️ Security Features

- **Offline-Only Operation**: No internet access required after initial setup
- **Input Validation**: Comprehensive security validation for all file operations
- **Secure Defaults**: TLS 1.2/1.3, secure file handling, and memory protection
- **Open Source**: Uses only permissively-licensed libraries (MIT, Apache-2.0, BSD)
- **No Vendor Lock-In**: All storage formats are open and portable

## 🏗️ Architecture

The application follows the **MVVM pattern** with clear separation of concerns:

```
SecurePdfEditor/
├── src/
│   ├── SecurePdfEditor/          # WPF Application (UI Layer)
│   ├── SecurePdfEditor.Core/     # Business Logic (Domain Layer)
│   └── SecurePdfEditor.Tests/    # Unit Tests (Quality Assurance)
├── NuGet.config                  # Package source configuration
├── Directory.Build.props         # Global build settings
└── README.md                     # This file
```

### Core Components

- **PdfDocument**: Secure PDF document handling with integrity verification
- **PdfDocumentValidator**: Security-focused input validation
- **MainWindowViewModel**: MVVM implementation for UI logic
- **Material Design**: Modern, accessible user interface

## 🚀 Getting Started

### Prerequisites

- **.NET 8.0 SDK** or later
- **Visual Studio 2022** (17.0+) or **Visual Studio Code**
- **Windows 10/11** (x64)

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd SecurePdfEditor
   ```

2. **Restore NuGet packages**
   ```bash
   dotnet restore
   ```

3. **Build the solution**
   ```bash
   dotnet build
   ```

4. **Run the application**
   ```bash
   dotnet run --project src/SecurePdfEditor
   ```

5. **Run tests**
   ```bash
   dotnet test
   ```

### Development Setup

1. **Open in Visual Studio 2022**
   - Open `SecurePdfEditor.sln`
   - Set `SecurePdfEditor` as the startup project
   - Press F5 to run

2. **Open in Visual Studio Code**
   - Install C# extension
   - Open the workspace
   - Use `Ctrl+Shift+P` → "Run and Debug"

## 📋 Features

### Current Features (Milestone 1)
- ✅ **Project Scaffolding**: Complete solution structure
- ✅ **Security Framework**: Input validation and secure defaults
- ✅ **MVVM Architecture**: Clean separation of concerns
- ✅ **Material Design UI**: Modern, accessible interface
- ✅ **Unit Testing**: xUnit tests with FluentAssertions
- ✅ **PDF Document Class**: Secure document handling foundation

### Planned Features
- 🔄 **PDF Viewing**: Native PDF rendering with PdfiumViewer
- 🔄 **Document Operations**: Open, save, and basic editing
- 🔄 **Security Features**: Encryption, password protection
- 🔄 **Content Editing**: Add text, images, and annotations
- 🔄 **Advanced Security**: Digital signatures, audit trails

## 🧪 Testing

The project includes comprehensive unit tests:

```bash
# Run all tests
dotnet test

# Run tests with coverage
dotnet test --collect:"XPlat Code Coverage"

# Run specific test project
dotnet test src/SecurePdfEditor.Tests
```

### Test Coverage
- **PdfDocument**: Document loading, validation, and integrity checks
- **Security Validation**: File path validation and security checks
- **MVVM Commands**: UI command functionality and state management

## 🔧 Configuration

### Build Settings (`Directory.Build.props`)
- **LangVersion**: `latest` (C# 12+ features)
- **Nullable**: `enable` (Null safety)
- **TreatWarningsAsErrors**: `true` (Code quality)
- **TargetFramework**: `net8.0-windows`

### NuGet Configuration (`NuGet.config`)
- **Package Source**: nuget.org only
- **Stable Releases**: Prefer stable over pre-release packages
- **Security**: No custom package sources

## 📦 Dependencies

### Core Libraries (Open Source Only)
- **CommunityToolkit.Mvvm** (MIT): MVVM framework
- **MaterialDesignThemes** (MIT): UI components
- **PdfiumViewer** (Apache-2.0): PDF rendering
- **iText7** (AGPL-3.0): PDF manipulation
- **FluentValidation** (Apache-2.0): Input validation

### Testing Libraries
- **xUnit** (Apache-2.0): Testing framework
- **FluentAssertions** (Apache-2.0): Test assertions
- **Moq** (BSD-3-Clause): Mocking framework

## 🔒 Security Considerations

### File Operations
- **Path Validation**: Prevents directory traversal attacks
- **File Size Limits**: 100MB maximum to prevent memory exhaustion
- **Extension Validation**: Only allows PDF files
- **Integrity Checks**: SHA-256 hashing for document verification

### Memory Management
- **Proper Disposal**: All resources implement IDisposable
- **Secure Cleanup**: Sensitive data cleared from memory
- **Exception Handling**: No sensitive information in error messages

### Network Security
- **Offline-Only**: No network calls after initial setup
- **TLS Configuration**: Secure defaults for any future network operations
- **No Telemetry**: No data collection or analytics

## 🤝 Contributing

### Development Guidelines
1. **Security-First**: All changes must maintain security standards
2. **Test Coverage**: New features require unit tests
3. **MVVM Pattern**: UI logic goes in ViewModels
4. **Documentation**: Code comments explain "why", not "what"
5. **Open Source Only**: No proprietary dependencies

### Code Style
- **C# 12+**: Use latest language features
- **Nullable Reference Types**: Enabled for all projects
- **Warnings as Errors**: Zero tolerance for warnings
- **Functional Design**: Prefer pure functions over classes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Common Issues

**Build Errors**
- Ensure .NET 8.0 SDK is installed
- Run `dotnet restore` before building
- Check that all NuGet packages are available

**Runtime Errors**
- Verify Windows 10/11 x64 compatibility
- Check file permissions for PDF operations
- Ensure sufficient disk space for temporary files

**Security Warnings**
- Review file paths for suspicious patterns
- Verify PDF file integrity before processing
- Check for unauthorized file access attempts

### Getting Help
1. Check the [Issues](../../issues) page for known problems
2. Review the test cases for usage examples
3. Examine the source code for implementation details

---

**⚠️ Security Notice**: This application is designed for offline use with security-first principles. Always validate files before processing and maintain proper access controls on your system.

