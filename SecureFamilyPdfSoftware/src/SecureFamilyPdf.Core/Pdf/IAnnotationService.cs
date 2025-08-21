using System.Drawing;

namespace SecureFamilyPdf.Core.Pdf;

/// <summary>
/// Service for adding annotations to PDF documents.
/// </summary>
public interface IAnnotationService
{
    /// <summary>
    /// Highlights text in the specified range with the given color.
    /// </summary>
    /// <param name="range">The text range to highlight</param>
    /// <param name="color">The highlight color</param>
    /// <returns>Task representing the asynchronous operation</returns>
    Task HighlightTextAsync(TextRange range, Color color);

    /// <summary>
    /// Adds a sticky note at the specified page position.
    /// </summary>
    /// <param name="pagePosition">The position on the page</param>
    /// <param name="text">The note text</param>
    /// <returns>Task representing the asynchronous operation</returns>
    Task AddStickyNoteAsync(Point pagePosition, string text);

    /// <summary>
    /// Saves all annotations to a new PDF file.
    /// </summary>
    /// <param name="outputPdfPath">The output PDF file path</param>
    /// <returns>Task representing the asynchronous operation</returns>
    Task SaveAnnotationsAsync(string outputPdfPath);
}

/// <summary>
/// Represents a range of text in a PDF document.
/// </summary>
public readonly struct TextRange
{
    public int PageIndex { get; }
    public int StartIndex { get; }
    public int EndIndex { get; }
    public RectangleF Bounds { get; }

    public TextRange(int pageIndex, int startIndex, int endIndex, RectangleF bounds)
    {
        PageIndex = pageIndex;
        StartIndex = startIndex;
        EndIndex = endIndex;
        Bounds = bounds;
    }
}

/// <summary>
/// Represents a 2D point with X and Y coordinates.
/// </summary>
public readonly struct Point
{
    public double X { get; }
    public double Y { get; }

    public Point(double x, double y)
    {
        X = x;
        Y = y;
    }
}
