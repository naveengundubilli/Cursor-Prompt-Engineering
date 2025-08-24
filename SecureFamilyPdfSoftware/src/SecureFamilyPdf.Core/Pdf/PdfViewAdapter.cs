using System.Windows.Media;
using System.Windows.Media.Imaging;
using Microsoft.Extensions.Logging;
using PdfiumViewer;
using SecureFamilyPdf.Core.Security;
using System.IO;

namespace SecureFamilyPdf.Core.Pdf;

/// <summary>
/// Adapter for PDF viewing that wraps PdfiumViewer for WPF integration.
/// Provides secure, offline PDF rendering with proper resource management.
/// </summary>
public sealed class PdfViewAdapter : IDisposable
{
    private readonly ILogger<PdfViewAdapter> _logger;
    private readonly PdfSecurityManager _securityManager;
    private PdfiumViewer.PdfDocument? _document;
    private bool _disposed;

    public PdfViewAdapter(ILogger<PdfViewAdapter> logger, PdfSecurityManager securityManager)
    {
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        _securityManager = securityManager ?? throw new ArgumentNullException(nameof(securityManager));
        
        // Initialize PdfiumViewer
        // Note: PdfLibrary.Load() is not accessible in this version
        // The library should be initialized automatically
    }

    /// <summary>
    /// Gets the total number of pages in the document.
    /// </summary>
    public int PageCount => _document?.PageCount ?? 0;

    /// <summary>
    /// Gets the current page index (0-based).
    /// </summary>
    public int CurrentPageIndex { get; private set; }

    /// <summary>
    /// Gets or sets the zoom level (1.0 = 100%).
    /// </summary>
    public double Zoom { get; set; } = 1.0;

    /// <summary>
    /// Gets whether a document is currently loaded.
    /// </summary>
    public bool IsDocumentLoaded => _document != null && !_disposed;

    /// <summary>
    /// Loads a PDF document from a local file path.
    /// </summary>
    /// <param name="filePath">Path to the PDF file</param>
    /// <returns>True if the document was loaded successfully</returns>
    public bool LoadDocument(string filePath)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(filePath);

        try
        {
            _logger.LogInformation("Loading PDF document: {FilePath}", filePath);

            // Validate the PDF file first
            var validationResult = _securityManager.ValidatePdfFile(filePath);
            if (!validationResult.IsValid)
            {
                _logger.LogError("PDF validation failed: {Errors}", string.Join(", ", validationResult.Errors));
                return false;
            }

            // Dispose of any existing document
            _document?.Dispose();

            // Load the new document
            _document = PdfiumViewer.PdfDocument.Load(filePath);
            
            // Apply security settings
            if (!_securityManager.ApplySecuritySettings(_document))
            {
                _logger.LogWarning("Failed to apply security settings to PDF document");
            }

            // Reset to first page
            CurrentPageIndex = 0;
            Zoom = 1.0;

            _logger.LogInformation("PDF document loaded successfully. Pages: {PageCount}", PageCount);
            return true;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to load PDF document: {FilePath}", filePath);
            return false;
        }
    }

    /// <summary>
    /// Renders the current page as a WPF ImageSource.
    /// </summary>
    /// <returns>The rendered page as an ImageSource, or null if no document is loaded</returns>
    public ImageSource? RenderCurrentPage()
    {
        if (_document == null || _disposed)
        {
            return null;
        }

        try
        {
            _logger.LogDebug("Rendering page {PageIndex} at zoom {Zoom}", CurrentPageIndex, Zoom);

            // Render the page using PdfiumViewer
            using var image = _document.Render(CurrentPageIndex, (float)(Zoom * 96), (float)(Zoom * 96), true);
            
            // Convert to WPF ImageSource
            var bitmap = new BitmapImage();
            using var stream = new MemoryStream();
            image.Save(stream, System.Drawing.Imaging.ImageFormat.Png);
            stream.Position = 0;
            
            bitmap.BeginInit();
            bitmap.CacheOption = BitmapCacheOption.OnLoad;
            bitmap.StreamSource = stream;
            bitmap.EndInit();
            bitmap.Freeze(); // Make it thread-safe

            return bitmap;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to render page {PageIndex}", CurrentPageIndex);
            return null;
        }
    }

    /// <summary>
    /// Navigates to the next page.
    /// </summary>
    /// <returns>True if navigation was successful</returns>
    public bool NextPage()
    {
        if (_document == null || _disposed || CurrentPageIndex >= PageCount - 1)
        {
            return false;
        }

        CurrentPageIndex++;
        _logger.LogDebug("Navigated to page {PageIndex}", CurrentPageIndex);
        return true;
    }

    /// <summary>
    /// Navigates to the previous page.
    /// </summary>
    /// <returns>True if navigation was successful</returns>
    public bool PreviousPage()
    {
        if (_document == null || _disposed || CurrentPageIndex <= 0)
        {
            return false;
        }

        CurrentPageIndex--;
        _logger.LogDebug("Navigated to page {PageIndex}", CurrentPageIndex);
        return true;
    }

    /// <summary>
    /// Navigates to a specific page by index.
    /// </summary>
    /// <param name="pageIndex">The page index (0-based)</param>
    /// <returns>True if navigation was successful</returns>
    public bool GoToPage(int pageIndex)
    {
        if (_document == null || _disposed || pageIndex < 0 || pageIndex >= PageCount)
        {
            _logger.LogWarning("Invalid page index: {PageIndex}. Valid range: 0-{MaxPage}", pageIndex, PageCount - 1);
            return false;
        }

        CurrentPageIndex = pageIndex;
        _logger.LogDebug("Navigated to page {PageIndex}", CurrentPageIndex);
        return true;
    }

    /// <summary>
    /// Gets the text content of the current page.
    /// </summary>
    /// <returns>The text content, or empty string if not available</returns>
    public string GetCurrentPageText()
    {
        if (_document == null || _disposed)
        {
            return string.Empty;
        }

        try
        {
            return _document.GetPdfText(CurrentPageIndex);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to extract text from page {PageIndex}", CurrentPageIndex);
            return string.Empty;
        }
    }

    /// <summary>
    /// Gets the dimensions of the current page.
    /// </summary>
    /// <returns>The page size in points, or null if not available</returns>
    public Size? GetCurrentPageSize()
    {
        if (_document == null || _disposed)
        {
            return null;
        }

        try
        {
            // Note: GetPageSize method is not available in this version of PdfiumViewer
        // For now, return a default size
        var size = new Size(612, 792); // Default A4 size in points
            return new Size(size.Width, size.Height);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to get page size for page {PageIndex}", CurrentPageIndex);
            return null;
        }
    }

    /// <summary>
    /// Closes the current document and releases resources.
    /// </summary>
    public void CloseDocument()
    {
        if (_document != null)
        {
            _logger.LogInformation("Closing PDF document");
            _document.Dispose();
            _document = null;
            CurrentPageIndex = 0;
        }
    }

    public void Dispose()
    {
        if (!_disposed)
        {
            _logger.LogDebug("Disposing PdfViewAdapter");
            CloseDocument();
            _disposed = true;
        }
    }
}

/// <summary>
/// Represents a 2D size with width and height.
/// </summary>
public struct Size
{
    public double Width { get; set; }
    public double Height { get; set; }

    public Size(double width, double height)
    {
        Width = width;
        Height = height;
    }
}
