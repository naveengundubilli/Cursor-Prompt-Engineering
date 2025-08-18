using System;
using System.Windows.Media.Imaging;

namespace SecurePdfEditor.Core.Pdf;

/// <summary>
/// Interface for PDF viewing operations that wraps PdfiumViewer for WPF integration.
/// </summary>
public interface IPdfViewAdapter : IDisposable
{
    /// <summary>
    /// Gets the total number of pages in the loaded PDF.
    /// </summary>
    int TotalPages { get; }
    
    /// <summary>
    /// Gets the current page number (1-based).
    /// </summary>
    int CurrentPage { get; }
    
    /// <summary>
    /// Gets whether a PDF is currently loaded.
    /// </summary>
    bool IsLoaded { get; }
    
    /// <summary>
    /// Gets the file path of the currently loaded PDF.
    /// </summary>
    string? FilePath { get; }
    
    /// <summary>
    /// Loads a PDF from the specified file path.
    /// </summary>
    /// <param name="filePath">The path to the PDF file.</param>
    /// <returns>True if the PDF was loaded successfully; otherwise, false.</returns>
    /// <exception cref="ArgumentException">Thrown when filePath is null or empty.</exception>
    /// <exception cref="FileNotFoundException">Thrown when the file does not exist.</exception>
    /// <exception cref="UnauthorizedAccessException">Thrown when access to the file is denied.</exception>
    bool LoadPdf(string filePath);
    
    /// <summary>
    /// Renders the current page as a WPF ImageSource.
    /// </summary>
    /// <param name="scaleFactor">The scale factor for rendering (default: 1.0).</param>
    /// <returns>The rendered page as an ImageSource, or null if no PDF is loaded.</returns>
    BitmapSource? RenderCurrentPage(double scaleFactor = 1.0);
    
    /// <summary>
    /// Navigates to the next page.
    /// </summary>
    /// <returns>True if navigation was successful; otherwise, false.</returns>
    bool NextPage();
    
    /// <summary>
    /// Navigates to the previous page.
    /// </summary>
    /// <returns>True if navigation was successful; otherwise, false.</returns>
    bool PreviousPage();
    
    /// <summary>
    /// Navigates to a specific page.
    /// </summary>
    /// <param name="pageNumber">The page number to navigate to (1-based).</param>
    /// <returns>True if navigation was successful; otherwise, false.</returns>
    /// <exception cref="ArgumentOutOfRangeException">Thrown when pageNumber is out of range.</exception>
    bool GoToPage(int pageNumber);
    
    /// <summary>
    /// Unloads the currently loaded PDF and releases associated resources.
    /// </summary>
    void UnloadPdf();
}



