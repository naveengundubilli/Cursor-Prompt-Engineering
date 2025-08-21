using System.Drawing;
using Microsoft.Extensions.Logging;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;

namespace SecureFamilyPdf.Core.Pdf;

/// <summary>
/// Implementation of the annotation service using QuestPDF for overlay-based annotations.
/// </summary>
public sealed class AnnotationService : IAnnotationService
{
    private readonly ILogger<AnnotationService> _logger;
    private readonly List<Annotation> _annotations = new();

    public AnnotationService(ILogger<AnnotationService> logger)
    {
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    /// <summary>
    /// Highlights text in the specified range with the given color.
    /// </summary>
    public async Task HighlightTextAsync(TextRange range, Color color)
    {
        try
        {
            _logger.LogInformation("Adding text highlight on page {PageIndex}", range.PageIndex);

            var annotation = new TextHighlightAnnotation
            {
                PageIndex = range.PageIndex,
                Bounds = range.Bounds,
                Color = color,
                TextRange = range
            };

            _annotations.Add(annotation);
            await Task.CompletedTask; // Async for future extensibility
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to add text highlight");
            throw;
        }
    }

    /// <summary>
    /// Adds a sticky note at the specified page position.
    /// </summary>
    public async Task AddStickyNoteAsync(Point pagePosition, string text)
    {
        try
        {
            _logger.LogInformation("Adding sticky note at position ({X}, {Y})", pagePosition.X, pagePosition.Y);

            var annotation = new StickyNoteAnnotation
            {
                PageIndex = 0, // Default to first page for now
                Position = pagePosition,
                Text = text,
                Bounds = new RectangleF((float)pagePosition.X, (float)pagePosition.Y, 20, 20)
            };

            _annotations.Add(annotation);
            await Task.CompletedTask; // Async for future extensibility
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to add sticky note");
            throw;
        }
    }

    /// <summary>
    /// Saves all annotations to a new PDF file using QuestPDF.
    /// </summary>
    public async Task SaveAnnotationsAsync(string outputPdfPath)
    {
        try
        {
            _logger.LogInformation("Saving annotations to: {OutputPath}", outputPdfPath);

            // Group annotations by page
            var annotationsByPage = _annotations.GroupBy(a => a.PageIndex).ToDictionary(g => g.Key, g => g.ToList());

            // Create PDF document with annotations
            var document = Document.Create(container =>
            {
                container.Page(page =>
                {
                    page.Size(PageSizes.A4);
                    page.Margin(1, Unit.Centimetre);
                    page.DefaultTextStyle(x => x.FontSize(12));

                    // Add page content (placeholder for now)
                    page.Content().Column(col =>
                    {
                        col.Item().Text("Annotated PDF Document");
                        col.Item().Text("This is a placeholder for the annotated content.");
                    });

                    // Add annotations as overlays
                    if (annotationsByPage.TryGetValue(0, out var pageAnnotations))
                    {
                        page.Overlay().Canvas((canvas, size) =>
                        {
                            foreach (var annotation in pageAnnotations)
                            {
                                DrawAnnotation(canvas, annotation);
                            }
                        });
                    }
                });
            });

            // Save the document
            document.GeneratePdf(outputPdfPath);
            await Task.CompletedTask; // Async for future extensibility

            _logger.LogInformation("Annotations saved successfully");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to save annotations");
            throw;
        }
    }

    /// <summary>
    /// Draws an annotation on the canvas.
    /// </summary>
    private void DrawAnnotation(ICanvas canvas, Annotation annotation)
    {
        switch (annotation)
        {
            case TextHighlightAnnotation highlight:
                DrawTextHighlight(canvas, highlight);
                break;
            case StickyNoteAnnotation stickyNote:
                DrawStickyNote(canvas, stickyNote);
                break;
        }
    }

    /// <summary>
    /// Draws a text highlight annotation.
    /// </summary>
    private void DrawTextHighlight(ICanvas canvas, TextHighlightAnnotation highlight)
    {
        var bounds = highlight.Bounds;
        var color = highlight.Color;

        canvas.SaveState();
        canvas.SetFillColor(ConvertColor(color));
        canvas.SetStrokeColor(ConvertColor(color));
        canvas.SetStrokeWidth(1);
        
        // Draw highlight rectangle
        canvas.DrawRectangle(bounds.X, bounds.Y, bounds.Width, bounds.Height);
        canvas.Fill();
        
        canvas.RestoreState();
    }

    /// <summary>
    /// Draws a sticky note annotation.
    /// </summary>
    private void DrawStickyNote(ICanvas canvas, StickyNoteAnnotation stickyNote)
    {
        var bounds = stickyNote.Bounds;
        var position = stickyNote.Position;

        canvas.SaveState();
        
        // Draw sticky note icon (yellow square)
        canvas.SetFillColor(Colors.Yellow);
        canvas.SetStrokeColor(Colors.Black);
        canvas.SetStrokeWidth(1);
        
        canvas.DrawRectangle((float)position.X, (float)position.Y, 20, 20);
        canvas.Fill();
        canvas.Stroke();
        
        // Draw note icon (small square)
        canvas.SetFillColor(Colors.Black);
        canvas.DrawRectangle((float)position.X + 5, (float)position.Y + 5, 10, 10);
        canvas.Fill();
        
        canvas.RestoreState();
    }

    /// <summary>
    /// Converts System.Drawing.Color to QuestPDF color.
    /// </summary>
    private static string ConvertColor(Color color)
    {
        return $"#{color.R:X2}{color.G:X2}{color.B:X2}";
    }

    /// <summary>
    /// Clears all annotations.
    /// </summary>
    public void ClearAnnotations()
    {
        _annotations.Clear();
        _logger.LogInformation("All annotations cleared");
    }

    /// <summary>
    /// Gets the count of annotations.
    /// </summary>
    public int AnnotationCount => _annotations.Count;
}

/// <summary>
/// Base class for all annotations.
/// </summary>
public abstract class Annotation
{
    public int PageIndex { get; set; }
    public RectangleF Bounds { get; set; }
}

/// <summary>
/// Text highlight annotation.
/// </summary>
public class TextHighlightAnnotation : Annotation
{
    public Color Color { get; set; }
    public TextRange TextRange { get; set; }
}

/// <summary>
/// Sticky note annotation.
/// </summary>
public class StickyNoteAnnotation : Annotation
{
    public Point Position { get; set; }
    public string Text { get; set; } = string.Empty;
}
