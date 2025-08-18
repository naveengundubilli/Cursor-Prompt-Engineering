Part 1 — Full Prompt Pack (Detailed)
Prompt 1 — Scaffold Solution & Repo Structure
[Constraints: see Global Constraints above]
You are acting as a senior C# WPF developer.  
Scaffold a new solution named "SecurePdfEditor" with:

- src/
  - SecurePdfEditor/ (WPF app project)
  - SecurePdfEditor.Core/ (class library for business logic)
  - SecurePdfEditor.Tests/ (xUnit test project)

Requirements:
- Use .NET 8.0
- Add solution‑level NuGet.config to prefer stable releases
- Configure Directory.Build.props to enforce LangVersion=latest, Nullable=enable, TreatWarningsAsErrors=true
- Pre‑wire src/SecurePdfEditor.Core with folders: Pdf, Security, Utils
- Add README.md with overview and local build instructions


Prompt 2 — PDF Viewing Adapter
[Constraints: see Global Constraints above]
Implement PdfViewAdapter in SecurePdfEditor.Core/Pdf that wraps PdfiumViewer for WPF:

- Load local PDFs from file path
- Render into WPF ImageSource for UI
- Support navigation: NextPage, PreviousPage, GoToPage(int)
- Dispose unmanaged resources
- Add xUnit tests mocking PdfiumViewer where possible


Prompt 3 — Annotation Service
[Constraints: see Global Constraints above]
Implement IAnnotationService in SecurePdfEditor.Core/Pdf:

- HighlightText(range, color)
- AddStickyNote(position, text)
- SaveAnnotations()

Use QuestPDF overlay approach, exporting as a new PDF.  
All writes must be offline.


Prompt 4 — Security Workflow
[Constraints: see Global Constraints above]
Create ISecurityService in SecurePdfEditor.Core/Security:

- ApplyPasswordProtection(pdfPath, password)
- RemovePassword(pdfPath, password)
- RedactText(searchTerm)

Integrate qpdf CLI via ProcessStartInfo.  
Add tests verifying password protection roundtrip + irreversible redaction.


Prompt 5 — WPF UI Integration
[Constraints: see Global Constraints above]
Bind PdfViewAdapter to MainWindow.xaml:

- Open file button
- Page navigation controls
- Annotation toolbar (highlight, sticky note)
- Security menu (password, redact)

Follow MVVM with MainViewModel, DataTemplates, Commands, INotifyPropertyChanged.


Prompt 6 — Offline Build & Test Automation
[Constraints: see Global Constraints above]
PowerShell scripts to:

- Restore packages from local NuGet cache
- Build + run tests
- Package binaries into /dist with README and LICENSE

Confirm workflow runs fully offline after cache is warmed.



Part 2 — Quick‑Access Index (Compact)
1 — Scaffold Solution & Repo Structure
[Constraints: see Global Constraints above]
Scaffold .NET 8 WPF solution "SecurePdfEditor" with:
- src/SecurePdfEditor (WPF)
- src/SecurePdfEditor.Core (class lib)
- src/SecurePdfEditor.Tests (xUnit)
+ NuGet.config, Directory.Build.props (LangVersion latest, Nullable enable, TreatWarningsAsErrors true)
+ README.md with build notes


2 — PDF Viewing Adapter
[Constraints: see Global Constraints above]
Implement PdfViewAdapter in Core/Pdf wrapping PdfiumViewer:
- Load local PDF → WPF ImageSource
- Navigation: Next, Prev, GoTo(int)
- Dispose unmanaged resources
- xUnit tests (mock PdfiumViewer)


3 — Annotation Service
[Constraints: see Global Constraints above]
IAnnotationService with:
- HighlightText(range, color)
- AddStickyNote(position, text)
- SaveAnnotations()
Use QuestPDF overlays, export new PDF, offline only


4 — Security Workflow
[Constraints: see Global Constraints above]
ISecurityService:
- ApplyPasswordProtection(path, pw)
- RemovePassword(path, pw)
- RedactText(searchTerm)
Integrate qpdf via ProcessStartInfo
Tests: pw roundtrip + irreversible redaction


5 — WPF UI Integration
[Constraints: see Global Constraints above]
Bind PdfViewAdapter to MainWindow.xaml:
- Open file
- Page navigation
- Annotation toolbar
- Security menu
MVVM: MainViewModel, DataTemplates, Commands


6 — Offline Build & Test Automation
[Constraints: see Global Constraints above]
PowerShell scripts to:
- Restore NuGet from local cache
- Build + test
- Package to /dist with README + LICENSE
Ensure offline run works after cache warm




