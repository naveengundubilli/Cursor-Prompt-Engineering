# Secure Family PDF Software

A secure, offline-first PDF viewer and editor for Windows built with .NET 8 WPF.

## 🛡️ Security Features

- **Offline by Design**: No network calls, telemetry, or online dependencies
- **JavaScript Disabled**: All PDF JavaScript execution is disabled by default
- **True Redaction**: Content removal, not just visual masking
- **Password Protection**: AES-256 encryption for sensitive documents
- **Sandboxed Rendering**: Secure PDF processing environment
- **File Validation**: Comprehensive PDF file integrity checks

## 🎨 Adobe-Style Interface

- **Menu Bar Layout**: Traditional Adobe-style menus (no top toolbar)
- **Thumbnail Panel**: Page thumbnails on the left side
- **Status Bar**: Bottom status bar with zoom controls and document info
- **Keyboard Shortcuts**: Full keyboard navigation support
- **Windows Native**: Follows Windows design guidelines

## 🏗️ Architecture

### Project Structure
```
SecureFamilyPdfSoftware/
├── src/
│   ├── SecureFamilyPdf/           # Main WPF application
│   └── SecureFamilyPdf.Core/      # Core PDF processing library
├── tests/
│   └── SecureFamilyPdf.Tests/     # Unit and integration tests
├── build/
│   └── scripts/                   # Build automation scripts
└── third_party/                   # Vendored native dependencies
```

### Technology Stack
- **.NET 8 WPF**: Modern Windows desktop framework
- **PdfiumViewer**: Secure PDF rendering engine
- **QuestPDF**: PDF generation and manipulation
- **qpdf**: Password protection and encryption
- **xUnit**: Testing framework
- **MSIX**: Modern Windows packaging

## 🚀 Getting Started

### Prerequisites

- **Windows 10/11** (64-bit)
- **.NET 8 SDK** (8.0.100 or later)
- **Visual Studio 2022** or **VS Code** with C# extension
- **PowerShell 5.1** or later

### Build Instructions

1. **Clone the repository**
   ```powershell
   git clone https://github.com/your-org/SecureFamilyPdfSoftware.git
   cd SecureFamilyPdfSoftware
   ```

2. **Restore dependencies**
   ```powershell
   dotnet restore
   ```

3. **Build the solution**
   ```powershell
   dotnet build --configuration Release
   ```

4. **Run tests**
   ```powershell
   dotnet test --configuration Release
   ```

5. **Run the application**
   ```powershell
   dotnet run --project src/SecureFamilyPdf --configuration Release
   ```

### Offline Build

The application is designed for offline operation. After the initial restore to warm the NuGet cache:

```powershell
# Build without network access
dotnet build --configuration Release --no-restore
```

## 📦 Packaging

### MSIX Package
```powershell
dotnet publish src/SecureFamilyPdf --configuration Release --runtime win-x64 --self-contained true --output ./publish
```

### Portable Single-File
```powershell
dotnet publish src/SecureFamilyPdf --configuration Release --runtime win-x64 --self-contained true --publish-single-file true --output ./publish
```

## 🔧 Development

### Key Features Implementation

#### PDF Viewing Adapter
- Wraps PdfiumViewer for WPF integration
- Secure resource management with proper disposal
- Page navigation and zoom controls
- Text extraction and search capabilities

#### Annotation Service
- Text highlighting with color selection
- Sticky note annotations
- Overlay-based annotation system using QuestPDF
- Non-destructive annotation export

#### Security Service
- Password protection using qpdf (AES-256)
- True redaction via rasterize-and-rebuild
- File validation and integrity checks
- Secure memory management

### Testing Strategy

- **Unit Tests**: Core business logic and security features
- **Integration Tests**: PDF processing workflows
- **UI Tests**: WPF application behavior
- **Security Tests**: Validation and redaction verification

## 🔒 Security Considerations

### Offline-First Design
- No telemetry or analytics collection
- No automatic update checks
- No cloud integration or sync
- All processing done locally

### PDF Security
- JavaScript execution disabled
- External links and network access blocked
- Embedded files and attachments disabled
- Form submission disabled
- File size and format validation

### Memory Safety
- Deterministic resource disposal
- Secure string handling
- No sensitive data logging
- Protected memory for passwords

## 📋 Requirements

### Functional Requirements
- [x] Load and display PDF documents
- [x] Page navigation and zoom controls
- [x] Text highlighting and annotations
- [x] Password protection and removal
- [x] True text redaction
- [x] Adobe-style user interface
- [x] Keyboard shortcuts
- [x] Thumbnail panel
- [x] Status bar with document info

### Non-Functional Requirements
- [x] Offline operation
- [x] Windows 10/11 compatibility
- [x] 64-bit architecture
- [x] Deterministic builds
- [x] Comprehensive testing
- [x] Security-first design
- [x] Performance optimization

## 🧪 Testing

### Running Tests
```powershell
# Run all tests
dotnet test

# Run with coverage
dotnet test --collect:"XPlat Code Coverage"

# Run specific test project
dotnet test tests/SecureFamilyPdf.Tests/
```

### Test Categories
- **Unit Tests**: Individual component testing
- **Integration Tests**: PDF processing workflows
- **Security Tests**: Validation and redaction
- **UI Tests**: WPF application behavior

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Development Guidelines
- Follow the existing code style
- Add unit tests for new features
- Ensure offline-first architecture
- Maintain security standards
- Update documentation

## 🐛 Troubleshooting

### Common Issues

**Build fails with missing dependencies**
```powershell
dotnet restore --force
```

**PDF rendering issues**
- Ensure PdfiumViewer native libraries are present
- Check file permissions and antivirus exclusions

**Security validation failures**
- Verify PDF file integrity
- Check file size limits
- Ensure proper file extensions

### Support

For issues and questions:
1. Check the troubleshooting section
2. Review existing issues
3. Create a new issue with detailed information

## 📈 Roadmap

### Milestone 1: Foundation ✅
- [x] Project structure and build pipeline
- [x] Basic PDF viewing capabilities
- [x] Security framework

### Milestone 2: Core Features
- [ ] Advanced annotation tools
- [ ] Search functionality
- [ ] Bookmark support

### Milestone 3: Advanced Features
- [ ] Print and export capabilities
- [ ] Accessibility features
- [ ] File association handling

### Milestone 4: Polish
- [ ] Performance optimization
- [ ] User experience improvements
- [ ] Comprehensive documentation

---

**Note**: This application is designed for secure, offline PDF processing. All operations are performed locally with no network dependencies.
