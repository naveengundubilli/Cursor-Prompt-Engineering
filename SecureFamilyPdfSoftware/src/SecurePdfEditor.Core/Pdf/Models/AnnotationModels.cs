using System;
using System.Drawing;

namespace SecurePdfEditor.Core.Pdf.Models;

/// <summary>
/// Represents a text range for highlighting annotations.
/// </summary>
public readonly struct TextRange : IEquatable<TextRange>
{
    /// <summary>
    /// Gets the page number (1-based).
    /// </summary>
    public int PageNumber { get; }
    
    /// <summary>
    /// Gets the starting character index.
    /// </summary>
    public int StartIndex { get; }
    
    /// <summary>
    /// Gets the ending character index (exclusive).
    /// </summary>
    public int EndIndex { get; }
    
    /// <summary>
    /// Gets the length of the text range.
    /// </summary>
    public int Length => EndIndex - StartIndex;
    
    /// <summary>
    /// Initializes a new instance of the TextRange struct.
    /// </summary>
    /// <param name="pageNumber">The page number (1-based).</param>
    /// <param name="startIndex">The starting character index.</param>
    /// <param name="endIndex">The ending character index (exclusive).</param>
    public TextRange(int pageNumber, int startIndex, int endIndex)
    {
        if (pageNumber < 1)
            throw new ArgumentOutOfRangeException(nameof(pageNumber), "Page number must be 1 or greater.");
        
        if (startIndex < 0)
            throw new ArgumentOutOfRangeException(nameof(startIndex), "Start index must be 0 or greater.");
        
        if (endIndex < startIndex)
            throw new ArgumentOutOfRangeException(nameof(endIndex), "End index must be greater than or equal to start index.");
        
        PageNumber = pageNumber;
        StartIndex = startIndex;
        EndIndex = endIndex;
    }
    
    /// <summary>
    /// Determines if this range overlaps with another range.
    /// </summary>
    /// <param name="other">The other text range to check.</param>
    /// <returns>True if the ranges overlap; otherwise, false.</returns>
    public bool Overlaps(TextRange other)
    {
        return PageNumber == other.PageNumber && 
               StartIndex < other.EndIndex && 
               EndIndex > other.StartIndex;
    }
    
    /// <summary>
    /// Determines if this range contains the specified index.
    /// </summary>
    /// <param name="index">The index to check.</param>
    /// <returns>True if the range contains the index; otherwise, false.</returns>
    public bool Contains(int index)
    {
        return index >= StartIndex && index < EndIndex;
    }
    
    /// <summary>
    /// Determines whether the specified object is equal to the current object.
    /// </summary>
    /// <param name="obj">The object to compare with the current object.</param>
    /// <returns>True if the specified object is equal to the current object; otherwise, false.</returns>
    public override bool Equals(object? obj)
    {
        return obj is TextRange range && Equals(range);
    }
    
    /// <summary>
    /// Determines whether the specified TextRange is equal to the current TextRange.
    /// </summary>
    /// <param name="other">The TextRange to compare with the current TextRange.</param>
    /// <returns>True if the specified TextRange is equal to the current TextRange; otherwise, false.</returns>
    public bool Equals(TextRange other)
    {
        return PageNumber == other.PageNumber &&
               StartIndex == other.StartIndex &&
               EndIndex == other.EndIndex;
    }
    
    /// <summary>
    /// Returns the hash code for this TextRange.
    /// </summary>
    /// <returns>A hash code for the current TextRange.</returns>
    public override int GetHashCode()
    {
        return HashCode.Combine(PageNumber, StartIndex, EndIndex);
    }
    
    /// <summary>
    /// Determines whether two TextRange instances are equal.
    /// </summary>
    /// <param name="left">The first TextRange to compare.</param>
    /// <param name="right">The second TextRange to compare.</param>
    /// <returns>True if the two TextRange instances are equal; otherwise, false.</returns>
    public static bool operator ==(TextRange left, TextRange right)
    {
        return left.Equals(right);
    }
    
    /// <summary>
    /// Determines whether two TextRange instances are not equal.
    /// </summary>
    /// <param name="left">The first TextRange to compare.</param>
    /// <param name="right">The second TextRange to compare.</param>
    /// <returns>True if the two TextRange instances are not equal; otherwise, false.</returns>
    public static bool operator !=(TextRange left, TextRange right)
    {
        return !(left == right);
    }
}

/// <summary>
/// Represents a position for sticky note annotations.
/// </summary>
public readonly struct AnnotationPosition : IEquatable<AnnotationPosition>
{
    /// <summary>
    /// Gets the page number (1-based).
    /// </summary>
    public int PageNumber { get; }
    
    /// <summary>
    /// Gets the X coordinate in points (1/72 inch).
    /// </summary>
    public float X { get; }
    
    /// <summary>
    /// Gets the Y coordinate in points (1/72 inch).
    /// </summary>
    public float Y { get; }
    
    /// <summary>
    /// Initializes a new instance of the AnnotationPosition struct.
    /// </summary>
    /// <param name="pageNumber">The page number (1-based).</param>
    /// <param name="x">The X coordinate in points.</param>
    /// <param name="y">The Y coordinate in points.</param>
    public AnnotationPosition(int pageNumber, float x, float y)
    {
        if (pageNumber < 1)
            throw new ArgumentOutOfRangeException(nameof(pageNumber), "Page number must be 1 or greater.");
        
        PageNumber = pageNumber;
        X = x;
        Y = y;
    }
    
    /// <summary>
    /// Determines whether the specified object is equal to the current object.
    /// </summary>
    /// <param name="obj">The object to compare with the current object.</param>
    /// <returns>True if the specified object is equal to the current object; otherwise, false.</returns>
    public override bool Equals(object? obj)
    {
        return obj is AnnotationPosition position && Equals(position);
    }
    
    /// <summary>
    /// Determines whether the specified AnnotationPosition is equal to the current AnnotationPosition.
    /// </summary>
    /// <param name="other">The AnnotationPosition to compare with the current AnnotationPosition.</param>
    /// <returns>True if the specified AnnotationPosition is equal to the current AnnotationPosition; otherwise, false.</returns>
    public bool Equals(AnnotationPosition other)
    {
        return PageNumber == other.PageNumber &&
               X.Equals(other.X) &&
               Y.Equals(other.Y);
    }
    
    /// <summary>
    /// Returns the hash code for this AnnotationPosition.
    /// </summary>
    /// <returns>A hash code for the current AnnotationPosition.</returns>
    public override int GetHashCode()
    {
        return HashCode.Combine(PageNumber, X, Y);
    }
    
    /// <summary>
    /// Determines whether two AnnotationPosition instances are equal.
    /// </summary>
    /// <param name="left">The first AnnotationPosition to compare.</param>
    /// <param name="right">The second AnnotationPosition to compare.</param>
    /// <returns>True if the two AnnotationPosition instances are equal; otherwise, false.</returns>
    public static bool operator ==(AnnotationPosition left, AnnotationPosition right)
    {
        return left.Equals(right);
    }
    
    /// <summary>
    /// Determines whether two AnnotationPosition instances are not equal.
    /// </summary>
    /// <param name="left">The first AnnotationPosition to compare.</param>
    /// <param name="right">The second AnnotationPosition to compare.</param>
    /// <returns>True if the two AnnotationPosition instances are not equal; otherwise, false.</returns>
    public static bool operator !=(AnnotationPosition left, AnnotationPosition right)
    {
        return !(left == right);
    }
}

/// <summary>
/// Represents a text highlight annotation.
/// </summary>
public class TextHighlightAnnotation
{
    /// <summary>
    /// Gets the unique identifier for this annotation.
    /// </summary>
    public Guid Id { get; }
    
    /// <summary>
    /// Gets the text range to highlight.
    /// </summary>
    public TextRange Range { get; }
    
    /// <summary>
    /// Gets the highlight color.
    /// </summary>
    public Color Color { get; }
    
    /// <summary>
    /// Gets the creation timestamp.
    /// </summary>
    public DateTime CreatedAt { get; }
    
    /// <summary>
    /// Initializes a new instance of the TextHighlightAnnotation class.
    /// </summary>
    /// <param name="range">The text range to highlight.</param>
    /// <param name="color">The highlight color.</param>
    public TextHighlightAnnotation(TextRange range, Color color)
    {
        Id = Guid.NewGuid();
        Range = range;
        Color = color;
        CreatedAt = DateTime.UtcNow;
    }
}

/// <summary>
/// Represents a sticky note annotation.
/// </summary>
public class StickyNoteAnnotation
{
    /// <summary>
    /// Gets the unique identifier for this annotation.
    /// </summary>
    public Guid Id { get; }
    
    /// <summary>
    /// Gets the position of the sticky note.
    /// </summary>
    public AnnotationPosition Position { get; }
    
    /// <summary>
    /// Gets the text content of the sticky note.
    /// </summary>
    public string Text { get; }
    
    /// <summary>
    /// Gets the creation timestamp.
    /// </summary>
    public DateTime CreatedAt { get; }
    
    /// <summary>
    /// Initializes a new instance of the StickyNoteAnnotation class.
    /// </summary>
    /// <param name="position">The position of the sticky note.</param>
    /// <param name="text">The text content.</param>
    public StickyNoteAnnotation(AnnotationPosition position, string text)
    {
        if (string.IsNullOrWhiteSpace(text))
            throw new ArgumentException("Text cannot be null or empty.", nameof(text));
        
        Id = Guid.NewGuid();
        Position = position;
        Text = text;
        CreatedAt = DateTime.UtcNow;
    }
}
