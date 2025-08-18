using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;
using SecurePdfEditor.Core.Pdf.Models;

namespace SecurePdfEditor.Core.Pdf;

/// <summary>
/// Implementation of IAnnotationService using QuestPDF for offline PDF annotation.
/// Uses overlay approach to create annotated PDFs without modifying the original.
/// </summary>
public class AnnotationService : IAnnotationService
{
    private bool _disposed;
    private string? _filePath;
    private int _totalPages;
    private readonly List<TextHighlightAnnotation> _textHighlights;
    private readonly List<StickyNoteAnnotation> _stickyNotes;
    
    /// <summary>
    /// Initializes a new instance of the AnnotationService class.
    /// </summary>
    public AnnotationService()
    {
        _textHighlights = new List<TextHighlightAnnotation>();
        _stickyNotes = new List<StickyNoteAnnotation>();
    }
    
    /// <summary>
    /// Gets whether a PDF is currently loaded for annotation.
    /// </summary>
    public bool IsLoaded => !string.IsNullOrEmpty(_filePath) && !_disposed;
    
    /// <summary>
    /// Gets the file path of the currently loaded PDF.
    /// </summary>
    public string? FilePath => _filePath;
    
    /// <summary>
    /// Gets the total number of pages in the loaded PDF.
    /// </summary>
    public int TotalPages => _totalPages;
    
    /// <summary>
    /// Gets all text highlight annotations.
    /// </summary>
    public IReadOnlyList<TextHighlightAnnotation> TextHighlights => _textHighlights.AsReadOnly();
    
    /// <summary>
    /// Gets all sticky note annotations.
    /// </summary>
    public IReadOnlyList<StickyNoteAnnotation> StickyNotes => _stickyNotes.AsReadOnly();
    
    /// <summary>
    /// Loads a PDF for annotation operations.
    /// </summary>
    /// <param name="filePath">The path to the PDF file.</param>
    /// <returns>True if the PDF was loaded successfully; otherwise, false.</returns>
    public bool LoadPdf(string filePath)
    {
        if (string.IsNullOrWhiteSpace(filePath))
        {
            throw new ArgumentException("File path cannot be null or empty.", nameof(filePath));
        }
        
        // Validate file path for security first
        if (!PdfDocumentValidator.ValidateFilePath(filePath))
        {
            throw new ArgumentException("Invalid or potentially dangerous file path.", nameof(filePath));
        }
        
        if (!File.Exists(filePath))
        {
            throw new FileNotFoundException($"PDF file not found: {filePath}", filePath);
        }
        
        try
        {
            // Unload any existing PDF
            UnloadPdf();
            
            // TODO: In future milestone, integrate with actual PDF library for page count
            // For now, simulate loading by reading file info
            var fileInfo = new FileInfo(filePath);
            if (fileInfo.Length == 0)
            {
                throw new InvalidOperationException("PDF file is empty.");
            }
            
            // Simulate page count based on file size (placeholder logic)
            _totalPages = Math.Max(1, (int)(fileInfo.Length / 100)); // Rough estimate for testing
            _filePath = filePath;
            
            return true;
        }
        catch (UnauthorizedAccessException)
        {
            throw;
        }
        catch (Exception ex)
        {
            throw new InvalidOperationException($"Failed to load PDF: {ex.Message}", ex);
        }
    }
    
    /// <summary>
    /// Highlights text in the specified range with the given color.
    /// </summary>
    /// <param name="range">The text range to highlight.</param>
    /// <param name="color">The highlight color.</param>
    /// <returns>The created text highlight annotation.</returns>
    public TextHighlightAnnotation HighlightText(TextRange range, Color color)
    {
        if (!IsLoaded)
        {
            throw new InvalidOperationException("No PDF is loaded for annotation.");
        }
        
        if (range.PageNumber < 1 || range.PageNumber > TotalPages)
        {
            throw new ArgumentOutOfRangeException(nameof(range), 
                $"Page number must be between 1 and {TotalPages}.");
        }
        
        // Check for overlapping highlights on the same page
        var overlapping = _textHighlights.Any(h => h.Range.Overlaps(range));
        if (overlapping)
        {
            throw new InvalidOperationException("Text range overlaps with existing highlight.");
        }
        
        var annotation = new TextHighlightAnnotation(range, color);
        _textHighlights.Add(annotation);
        
        return annotation;
    }
    
    /// <summary>
    /// Adds a sticky note at the specified position.
    /// </summary>
    /// <param name="position">The position for the sticky note.</param>
    /// <param name="text">The text content of the sticky note.</param>
    /// <returns>The created sticky note annotation.</returns>
    public StickyNoteAnnotation AddStickyNote(AnnotationPosition position, string text)
    {
        if (!IsLoaded)
        {
            throw new InvalidOperationException("No PDF is loaded for annotation.");
        }
        
        if (position.PageNumber < 1 || position.PageNumber > TotalPages)
        {
            throw new ArgumentOutOfRangeException(nameof(position), 
                $"Page number must be between 1 and {TotalPages}.");
        }
        
        if (string.IsNullOrWhiteSpace(text))
        {
            throw new ArgumentException("Text cannot be null or empty.", nameof(text));
        }
        
        var annotation = new StickyNoteAnnotation(position, text);
        _stickyNotes.Add(annotation);
        
        return annotation;
    }
    
    /// <summary>
    /// Removes a text highlight annotation.
    /// </summary>
    /// <param name="annotationId">The ID of the annotation to remove.</param>
    /// <returns>True if the annotation was removed; otherwise, false.</returns>
    public bool RemoveTextHighlight(Guid annotationId)
    {
        var annotation = _textHighlights.FirstOrDefault(h => h.Id == annotationId);
        if (annotation != null)
        {
            _textHighlights.Remove(annotation);
            return true;
        }
        return false;
    }
    
    /// <summary>
    /// Removes a sticky note annotation.
    /// </summary>
    /// <param name="annotationId">The ID of the annotation to remove.</param>
    /// <returns>True if the annotation was removed; otherwise, false.</returns>
    public bool RemoveStickyNote(Guid annotationId)
    {
        var annotation = _stickyNotes.FirstOrDefault(s => s.Id == annotationId);
        if (annotation != null)
        {
            _stickyNotes.Remove(annotation);
            return true;
        }
        return false;
    }
    
    /// <summary>
    /// Saves all annotations to a new PDF file using QuestPDF overlay approach.
    /// </summary>
    /// <param name="outputPath">The path where the annotated PDF will be saved.</param>
    /// <returns>True if the annotations were saved successfully; otherwise, false.</returns>
    public bool SaveAnnotations(string outputPath)
    {
        if (!IsLoaded)
        {
            throw new InvalidOperationException("No PDF is loaded for annotation.");
        }
        
        if (string.IsNullOrWhiteSpace(outputPath))
        {
            throw new ArgumentException("Output path cannot be null or empty.", nameof(outputPath));
        }
        
        // Validate output path for security
        if (!PdfDocumentValidator.ValidateFilePath(outputPath))
        {
            throw new ArgumentException("Invalid or potentially dangerous output path.", nameof(outputPath));
        }
        
        try
        {
            // TODO: In future milestone, implement actual PDF overlay with QuestPDF
            // For now, create a placeholder PDF with annotation information
            CreateAnnotatedPdf(outputPath);
            
            return true;
        }
        catch (UnauthorizedAccessException)
        {
            throw;
        }
        catch (Exception ex)
        {
            throw new InvalidOperationException($"Failed to save annotations: {ex.Message}", ex);
        }
    }
    
    /// <summary>
    /// Saves all annotations to a new PDF file asynchronously using QuestPDF overlay approach.
    /// </summary>
    /// <param name="outputPath">The path where the annotated PDF will be saved.</param>
    /// <returns>A task that represents the asynchronous save operation.</returns>
    public async Task<bool> SaveAnnotationsAsync(string outputPath)
    {
        return await Task.Run(() => SaveAnnotations(outputPath)).ConfigureAwait(false);
    }
    
    /// <summary>
    /// Clears all annotations without saving.
    /// </summary>
    public void ClearAnnotations()
    {
        _textHighlights.Clear();
        _stickyNotes.Clear();
    }
    
    /// <summary>
    /// Unloads the currently loaded PDF and releases associated resources.
    /// </summary>
    public void UnloadPdf()
    {
        _filePath = null;
        _totalPages = 0;
        ClearAnnotations();
    }
    
    /// <summary>
    /// Creates an annotated PDF using QuestPDF with overlay approach.
    /// TODO: In future milestone, implement actual PDF overlay with original PDF content.
    /// </summary>
    /// <param name="outputPath">The path where the annotated PDF will be saved.</param>
    private void CreateAnnotatedPdf(string outputPath)
    {
        // Create a document with annotation information
        var document = Document.Create(container =>
        {
            container.Page(page =>
            {
                page.Size(PageSizes.A4);
                page.Margin(2, Unit.Centimetre);
                page.DefaultTextStyle(x => x.FontSize(12));
                
                page.Header().Text("Secure PDF Editor - Annotated Document")
                    .SemiBold().FontSize(16).FontColor(Colors.Blue.Medium);
                
                page.Content().Column(col =>
                {
                    col.Item().Text($"Original File: {Path.GetFileName(_filePath)}")
                        .FontSize(10).FontColor(Colors.Grey.Medium);
                    
                    col.Item().Text($"Total Pages: {TotalPages}")
                        .FontSize(10).FontColor(Colors.Grey.Medium);
                    
                    col.Item().Text($"Text Highlights: {_textHighlights.Count}")
                        .FontSize(10).FontColor(Colors.Grey.Medium);
                    
                    col.Item().Text($"Sticky Notes: {_stickyNotes.Count}")
                        .FontSize(10).FontColor(Colors.Grey.Medium);
                    
                    // Add text highlights information
                    if (_textHighlights.Count > 0)
                    {
                        col.Item().Text("Text Highlights:").Bold().FontSize(14);
                        
                        foreach (var highlight in _textHighlights)
                        {
                            col.Item().Background(GetColorString(highlight.Color)).Padding(5).Text(
                                $"Page {highlight.Range.PageNumber}: " +
                                $"Range {highlight.Range.StartIndex}-{highlight.Range.EndIndex} " +
                                $"(Length: {highlight.Range.Length})")
                                .FontSize(10);
                        }
                    }
                    
                    // Add sticky notes information
                    if (_stickyNotes.Count > 0)
                    {
                        col.Item().Text("Sticky Notes:").Bold().FontSize(14);
                        
                        foreach (var note in _stickyNotes)
                        {
                            col.Item().Border(1).BorderColor(Colors.Yellow.Medium).Padding(5).Column(noteCol =>
                            {
                                noteCol.Item().Text($"Page {note.Position.PageNumber}: " +
                                    $"Position ({note.Position.X:F1}, {note.Position.Y:F1})")
                                    .FontSize(10).FontColor(Colors.Grey.Medium);
                                noteCol.Item().Text(note.Text).FontSize(10);
                            });
                        }
                    }
                    
                    // Add placeholder for original PDF content
                    col.Item().Text("Original PDF Content Placeholder")
                        .FontSize(12).FontColor(Colors.Grey.Medium)
                        .Italic();
                });
                
                page.Footer().AlignCenter().Text(x =>
                {
                    x.CurrentPageNumber();
                    x.Span(" / ");
                    x.TotalPages();
                });
            });
        });
        
        // Generate the PDF
        document.GeneratePdf(outputPath);
    }
    
    /// <summary>
    /// Converts a System.Drawing.Color to a QuestPDF color string.
    /// </summary>
    /// <param name="color">The System.Drawing.Color to convert.</param>
    /// <returns>The color as a hex string.</returns>
    private static string GetColorString(Color color)
    {
        return $"#{color.R:X2}{color.G:X2}{color.B:X2}";
    }
    
    /// <summary>
    /// Disposes the AnnotationService and releases all resources.
    /// </summary>
    public void Dispose()
    {
        Dispose(true);
        GC.SuppressFinalize(this);
    }
    
    /// <summary>
    /// Disposes the AnnotationService and releases all resources.
    /// </summary>
    /// <param name="disposing">True if called from Dispose; false if called from finalizer.</param>
    protected virtual void Dispose(bool disposing)
    {
        if (!_disposed && disposing)
        {
            UnloadPdf();
            _disposed = true;
        }
    }
}
