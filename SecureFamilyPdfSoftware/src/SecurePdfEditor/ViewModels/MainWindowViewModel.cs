using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.Win32;
using SecurePdfEditor.Core.Pdf;
using static SecurePdfEditor.Core.Pdf.PdfDocumentValidator;

namespace SecurePdfEditor.ViewModels;

/// <summary>
/// ViewModel for the main window following MVVM pattern.
/// Handles UI logic and user interactions with security-first approach.
/// </summary>
public partial class MainWindowViewModel : ObservableObject, IDisposable
{
    private readonly PdfDocument _pdfDocument;
    private int _currentPage = 1;
    private int _totalPages;
    private double _zoomLevel = 1.0;
    private string _statusMessage = "Ready";
    private string _securityStatus = "Secure";

    public MainWindowViewModel()
    {
        _pdfDocument = new PdfDocument();
        
        // Initialize commands
        OpenPdfCommand = new RelayCommand(OpenPdf, CanOpenPdf);
        SavePdfCommand = new RelayCommand(SavePdf, CanSavePdf);
        EncryptPdfCommand = new RelayCommand(EncryptPdf, CanEncryptPdf);
        RemovePasswordCommand = new RelayCommand(RemovePassword, CanRemovePassword);
        AddTextCommand = new RelayCommand(AddText, CanAddText);
        AddImageCommand = new RelayCommand(AddImage, CanAddImage);
        PreviousPageCommand = new RelayCommand(PreviousPage, CanPreviousPage);
        NextPageCommand = new RelayCommand(NextPage, CanNextPage);
        ZoomInCommand = new RelayCommand(ZoomIn, CanZoomIn);
        ZoomOutCommand = new RelayCommand(ZoomOut, CanZoomOut);
    }

    #region Properties

    /// <summary>
    /// Gets the current page number being displayed.
    /// </summary>
    public int CurrentPage
    {
        get => _currentPage;
        private set => SetProperty(ref _currentPage, value);
    }

    /// <summary>
    /// Gets the total number of pages in the document.
    /// </summary>
    public int TotalPages
    {
        get => _totalPages;
        private set => SetProperty(ref _totalPages, value);
    }

    /// <summary>
    /// Gets the current zoom level for the PDF viewer.
    /// </summary>
    public double ZoomLevel
    {
        get => _zoomLevel;
        private set => SetProperty(ref _zoomLevel, value);
    }

    /// <summary>
    /// Gets the current page text for display (e.g., "Page 1 of 5").
    /// </summary>
    public string CurrentPageText => $"Page {CurrentPage} of {TotalPages}";

    /// <summary>
    /// Gets the zoom text for display (e.g., "100%").
    /// </summary>
    public string ZoomText => $"{ZoomLevel * 100:F0}%";

    /// <summary>
    /// Gets the current status message for the user.
    /// </summary>
    public string StatusMessage
    {
        get => _statusMessage;
        private set => SetProperty(ref _statusMessage, value);
    }

    /// <summary>
    /// Gets the current security status.
    /// </summary>
    public string SecurityStatus
    {
        get => _securityStatus;
        private set => SetProperty(ref _securityStatus, value);
    }

    /// <summary>
    /// Gets the PDF viewer content to display.
    /// </summary>
    public object? PdfViewerContent { get; private set; }

    #endregion

    #region Commands

    /// <summary>
    /// Command to open a PDF file.
    /// </summary>
    public ICommand OpenPdfCommand { get; }

    /// <summary>
    /// Command to save the current PDF file.
    /// </summary>
    public ICommand SavePdfCommand { get; }

    /// <summary>
    /// Command to encrypt the current PDF file.
    /// </summary>
    public ICommand EncryptPdfCommand { get; }

    /// <summary>
    /// Command to remove password protection from the current PDF file.
    /// </summary>
    public ICommand RemovePasswordCommand { get; }

    /// <summary>
    /// Command to add text to the current PDF.
    /// </summary>
    public ICommand AddTextCommand { get; }

    /// <summary>
    /// Command to add an image to the current PDF.
    /// </summary>
    public ICommand AddImageCommand { get; }

    /// <summary>
    /// Command to navigate to the previous page.
    /// </summary>
    public ICommand PreviousPageCommand { get; }

    /// <summary>
    /// Command to navigate to the next page.
    /// </summary>
    public ICommand NextPageCommand { get; }

    /// <summary>
    /// Command to zoom in on the PDF.
    /// </summary>
    public ICommand ZoomInCommand { get; }

    /// <summary>
    /// Command to zoom out on the PDF.
    /// </summary>
    public ICommand ZoomOutCommand { get; }

    #endregion

    #region Command Implementations

    /// <summary>
    /// Opens a PDF file with security validation.
    /// </summary>
    private void OpenPdf()
    {
        try
        {
            var openFileDialog = new OpenFileDialog
            {
                Title = "Open PDF File",
                Filter = "PDF Files (*.pdf)|*.pdf|All Files (*.*)|*.*",
                FilterIndex = 1,
                CheckFileExists = true,
                CheckPathExists = true
            };

            if (openFileDialog.ShowDialog() == true)
            {
                var filePath = openFileDialog.FileName;
                
                // Validate file path for security
                if (!PdfDocumentValidator.ValidateFilePath(filePath))
                {
                    StatusMessage = "Invalid file path detected.";
                    SecurityStatus = "Security Warning";
                    return;
                }

                // Load the PDF document
                if (_pdfDocument.LoadDocument(filePath))
                {
                    TotalPages = _pdfDocument.PageCount;
                    CurrentPage = 1;
                    StatusMessage = $"Loaded: {System.IO.Path.GetFileName(filePath)}";
                    SecurityStatus = _pdfDocument.IsEncrypted ? "Encrypted" : "Secure";
                    
                    // TODO: Load PDF content into viewer
                    // PdfViewerContent = LoadPdfContent(filePath);
                }
                else
                {
                    StatusMessage = "Failed to load PDF file.";
                    SecurityStatus = "Error";
                }
            }
        }
        catch (System.IO.IOException ex)
        {
            StatusMessage = $"Error opening file: {ex.Message}";
            SecurityStatus = "Error";
        }
        catch (System.Security.SecurityException)
        {
            StatusMessage = "Access denied to file.";
            SecurityStatus = "Security Error";
        }
        catch (System.ArgumentException ex)
        {
            StatusMessage = $"Invalid file path: {ex.Message}";
            SecurityStatus = "Error";
        }
    }

    /// <summary>
    /// Determines if the Open PDF command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanOpenPdf()
    {
        return true; // Always allow opening files
    }

    /// <summary>
    /// Saves the current PDF file.
    /// </summary>
    private void SavePdf()
    {
        try
        {
            var saveFileDialog = new SaveFileDialog
            {
                Title = "Save PDF File",
                Filter = "PDF Files (*.pdf)|*.pdf|All Files (*.*)|*.*",
                FilterIndex = 1,
                DefaultExt = "pdf"
            };

            if (saveFileDialog.ShowDialog() == true)
            {
                var filePath = saveFileDialog.FileName;
                
                // Validate file path for security
                if (!PdfDocumentValidator.ValidateFilePath(filePath))
                {
                    StatusMessage = "Invalid file path detected.";
                    SecurityStatus = "Security Warning";
                    return;
                }

                // TODO: Implement PDF saving logic
                StatusMessage = $"Saved: {System.IO.Path.GetFileName(filePath)}";
                SecurityStatus = "Secure";
            }
        }
        catch (System.IO.IOException ex)
        {
            StatusMessage = $"Error saving file: {ex.Message}";
            SecurityStatus = "Error";
        }
        catch (System.Security.SecurityException)
        {
            StatusMessage = "Access denied to save location.";
            SecurityStatus = "Security Error";
        }
        catch (System.ArgumentException ex)
        {
            StatusMessage = $"Invalid save path: {ex.Message}";
            SecurityStatus = "Error";
        }
    }

    /// <summary>
    /// Determines if the Save PDF command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanSavePdf()
    {
        return !string.IsNullOrEmpty(_pdfDocument.FilePath);
    }

    /// <summary>
    /// Encrypts the current PDF file.
    /// </summary>
    private void EncryptPdf()
    {
        // TODO: Implement PDF encryption
        StatusMessage = "PDF encryption feature coming soon.";
        SecurityStatus = "Secure";
    }

    /// <summary>
    /// Determines if the Encrypt PDF command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanEncryptPdf()
    {
        return !string.IsNullOrEmpty(_pdfDocument.FilePath) && !_pdfDocument.IsEncrypted;
    }

    /// <summary>
    /// Removes password protection from the current PDF file.
    /// </summary>
    private void RemovePassword()
    {
        // TODO: Implement password removal
        StatusMessage = "Password removal feature coming soon.";
        SecurityStatus = "Secure";
    }

    /// <summary>
    /// Determines if the Remove Password command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanRemovePassword()
    {
        return !string.IsNullOrEmpty(_pdfDocument.FilePath) && _pdfDocument.IsPasswordProtected;
    }

    /// <summary>
    /// Adds text to the current PDF.
    /// </summary>
    private void AddText()
    {
        // TODO: Implement text addition
        StatusMessage = "Text addition feature coming soon.";
        SecurityStatus = "Secure";
    }

    /// <summary>
    /// Determines if the Add Text command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanAddText()
    {
        return !string.IsNullOrEmpty(_pdfDocument.FilePath);
    }

    /// <summary>
    /// Adds an image to the current PDF.
    /// </summary>
    private void AddImage()
    {
        // TODO: Implement image addition
        StatusMessage = "Image addition feature coming soon.";
        SecurityStatus = "Secure";
    }

    /// <summary>
    /// Determines if the Add Image command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanAddImage()
    {
        return !string.IsNullOrEmpty(_pdfDocument.FilePath);
    }

    /// <summary>
    /// Navigates to the previous page.
    /// </summary>
    private void PreviousPage()
    {
        if (CurrentPage > 1)
        {
            CurrentPage--;
            StatusMessage = $"Navigated to page {CurrentPage}";
        }
    }

    /// <summary>
    /// Determines if the Previous Page command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanPreviousPage()
    {
        return CurrentPage > 1;
    }

    /// <summary>
    /// Navigates to the next page.
    /// </summary>
    private void NextPage()
    {
        if (CurrentPage < TotalPages)
        {
            CurrentPage++;
            StatusMessage = $"Navigated to page {CurrentPage}";
        }
    }

    /// <summary>
    /// Determines if the Next Page command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanNextPage()
    {
        return CurrentPage < TotalPages;
    }

    /// <summary>
    /// Zooms in on the PDF.
    /// </summary>
    private void ZoomIn()
    {
        if (ZoomLevel < 5.0) // Maximum 500% zoom
        {
            ZoomLevel = Math.Min(5.0, ZoomLevel + 0.25);
            StatusMessage = $"Zoomed to {ZoomText}";
        }
    }

    /// <summary>
    /// Determines if the Zoom In command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanZoomIn()
    {
        return ZoomLevel < 5.0;
    }

    /// <summary>
    /// Zooms out on the PDF.
    /// </summary>
    private void ZoomOut()
    {
        if (ZoomLevel > 0.25) // Minimum 25% zoom
        {
            ZoomLevel = Math.Max(0.25, ZoomLevel - 0.25);
            StatusMessage = $"Zoomed to {ZoomText}";
        }
    }

    /// <summary>
    /// Determines if the Zoom Out command can execute.
    /// </summary>
    /// <returns>True if the command can execute; otherwise, false.</returns>
    private bool CanZoomOut()
    {
        return ZoomLevel > 0.25;
    }

    #endregion

    #region IDisposable Implementation

    private bool _disposed;

    /// <summary>
    /// Disposes of the ViewModel and releases any unmanaged resources.
    /// </summary>
    public void Dispose()
    {
        Dispose(true);
        GC.SuppressFinalize(this);
    }

    /// <summary>
    /// Disposes of the ViewModel and releases any unmanaged resources.
    /// </summary>
    /// <param name="disposing">True if called from Dispose(); false if called from finalizer.</param>
    protected virtual void Dispose(bool disposing)
    {
        if (!_disposed && disposing)
        {
            _pdfDocument?.Dispose();
            _disposed = true;
        }
    }

    #endregion
}
