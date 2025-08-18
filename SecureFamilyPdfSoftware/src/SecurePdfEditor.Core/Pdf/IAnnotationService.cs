using System;
using System.Collections.Generic;
using System.Drawing;
using System.Threading.Tasks;
using SecurePdfEditor.Core.Pdf.Models;

namespace SecurePdfEditor.Core.Pdf;

/// <summary>
/// Interface for PDF annotation operations using QuestPDF overlay approach.
/// Provides offline annotation capabilities with secure file handling.
/// </summary>
public interface IAnnotationService : IDisposable
{
    /// <summary>
    /// Gets whether a PDF is currently loaded for annotation.
    /// </summary>
    bool IsLoaded { get; }
    
    /// <summary>
    /// Gets the file path of the currently loaded PDF.
    /// </summary>
    string? FilePath { get; }
    
    /// <summary>
    /// Gets the total number of pages in the loaded PDF.
    /// </summary>
    int TotalPages { get; }
    
    /// <summary>
    /// Gets all text highlight annotations.
    /// </summary>
    IReadOnlyList<TextHighlightAnnotation> TextHighlights { get; }
    
    /// <summary>
    /// Gets all sticky note annotations.
    /// </summary>
    IReadOnlyList<StickyNoteAnnotation> StickyNotes { get; }
    
    /// <summary>
    /// Loads a PDF for annotation operations.
    /// </summary>
    /// <param name="filePath">The path to the PDF file.</param>
    /// <returns>True if the PDF was loaded successfully; otherwise, false.</returns>
    /// <exception cref="ArgumentException">Thrown when filePath is null or empty.</exception>
    /// <exception cref="FileNotFoundException">Thrown when the file does not exist.</exception>
    /// <exception cref="UnauthorizedAccessException">Thrown when access to the file is denied.</exception>
    bool LoadPdf(string filePath);
    
    /// <summary>
    /// Highlights text in the specified range with the given color.
    /// </summary>
    /// <param name="range">The text range to highlight.</param>
    /// <param name="color">The highlight color.</param>
    /// <returns>The created text highlight annotation.</returns>
    /// <exception cref="InvalidOperationException">Thrown when no PDF is loaded.</exception>
    /// <exception cref="ArgumentOutOfRangeException">Thrown when the range is invalid.</exception>
    TextHighlightAnnotation HighlightText(TextRange range, Color color);
    
    /// <summary>
    /// Adds a sticky note at the specified position.
    /// </summary>
    /// <param name="position">The position for the sticky note.</param>
    /// <param name="text">The text content of the sticky note.</param>
    /// <returns>The created sticky note annotation.</returns>
    /// <exception cref="InvalidOperationException">Thrown when no PDF is loaded.</exception>
    /// <exception cref="ArgumentException">Thrown when text is null or empty.</exception>
    /// <exception cref="ArgumentOutOfRangeException">Thrown when the position is invalid.</exception>
    StickyNoteAnnotation AddStickyNote(AnnotationPosition position, string text);
    
    /// <summary>
    /// Removes a text highlight annotation.
    /// </summary>
    /// <param name="annotationId">The ID of the annotation to remove.</param>
    /// <returns>True if the annotation was removed; otherwise, false.</returns>
    bool RemoveTextHighlight(Guid annotationId);
    
    /// <summary>
    /// Removes a sticky note annotation.
    /// </summary>
    /// <param name="annotationId">The ID of the annotation to remove.</param>
    /// <returns>True if the annotation was removed; otherwise, false.</returns>
    bool RemoveStickyNote(Guid annotationId);
    
    /// <summary>
    /// Saves all annotations to a new PDF file using QuestPDF overlay approach.
    /// </summary>
    /// <param name="outputPath">The path where the annotated PDF will be saved.</param>
    /// <returns>True if the annotations were saved successfully; otherwise, false.</returns>
    /// <exception cref="InvalidOperationException">Thrown when no PDF is loaded.</exception>
    /// <exception cref="ArgumentException">Thrown when outputPath is null or empty.</exception>
    /// <exception cref="UnauthorizedAccessException">Thrown when access to the output path is denied.</exception>
    bool SaveAnnotations(string outputPath);
    
    /// <summary>
    /// Saves all annotations to a new PDF file asynchronously using QuestPDF overlay approach.
    /// </summary>
    /// <param name="outputPath">The path where the annotated PDF will be saved.</param>
    /// <returns>A task that represents the asynchronous save operation.</returns>
    /// <exception cref="InvalidOperationException">Thrown when no PDF is loaded.</exception>
    /// <exception cref="ArgumentException">Thrown when outputPath is null or empty.</exception>
    /// <exception cref="UnauthorizedAccessException">Thrown when access to the output path is denied.</exception>
    Task<bool> SaveAnnotationsAsync(string outputPath);
    
    /// <summary>
    /// Clears all annotations without saving.
    /// </summary>
    void ClearAnnotations();
    
    /// <summary>
    /// Unloads the currently loaded PDF and releases associated resources.
    /// </summary>
    void UnloadPdf();
}



