using System;
using System.IO;
using System.Security.Cryptography;
using FluentValidation;

namespace SecurePdfEditor.Core.Pdf;

/// <summary>
/// Represents a PDF document with security-first design principles.
/// Handles document loading, validation, and secure operations.
/// </summary>
public class PdfDocument : IDisposable
{
    private readonly PdfDocumentValidator _validator;
    private bool _disposed;

    /// <summary>
    /// Gets the file path of the loaded PDF document.
    /// </summary>
    public string? FilePath { get; private set; }

    /// <summary>
    /// Gets the document size in bytes.
    /// </summary>
    public long FileSize { get; private set; }

    /// <summary>
    /// Gets the number of pages in the document.
    /// </summary>
    public int PageCount { get; private set; }

    /// <summary>
    /// Gets whether the document is password protected.
    /// </summary>
    public bool IsPasswordProtected { get; private set; }

    /// <summary>
    /// Gets whether the document is encrypted.
    /// </summary>
    public bool IsEncrypted { get; private set; }

    /// <summary>
    /// Gets the document's hash for integrity verification.
    /// </summary>
    public string? DocumentHash { get; private set; }

    /// <summary>
    /// Initializes a new instance of the PdfDocument class.
    /// </summary>
    public PdfDocument()
    {
        _validator = new PdfDocumentValidator();
    }

    /// <summary>
    /// Loads a PDF document from the specified file path with security validation.
    /// </summary>
    /// <param name="filePath">The path to the PDF file to load.</param>
    /// <param name="password">Optional password if the document is protected.</param>
    /// <returns>True if the document was loaded successfully; otherwise, false.</returns>
    /// <exception cref="ArgumentException">Thrown when filePath is null or empty.</exception>
    /// <exception cref="FileNotFoundException">Thrown when the specified file does not exist.</exception>
    /// <exception cref="ValidationException">Thrown when the file fails security validation.</exception>
    public bool LoadDocument(string filePath, string? password = null)
    {
        // Validate input parameters
        if (string.IsNullOrWhiteSpace(filePath))
        {
            throw new ArgumentException("File path cannot be null or empty.", nameof(filePath));
        }

        if (!File.Exists(filePath))
        {
            throw new FileNotFoundException($"PDF file not found: {filePath}", filePath);
        }

        try
        {
            // Get file information for security validation
            var fileInfo = new FileInfo(filePath);
            FileSize = fileInfo.Length;

            // Validate file size (prevent loading extremely large files that could cause memory issues)
            if (FileSize > 100 * 1024 * 1024) // 100MB limit
            {
                throw new ValidationException("File size exceeds maximum allowed size of 100MB.");
            }

            // Validate file extension
            if (!filePath.EndsWith(".pdf", StringComparison.OrdinalIgnoreCase))
            {
                throw new ValidationException("File must have a .pdf extension.");
            }

            // Calculate document hash for integrity verification
            DocumentHash = CalculateDocumentHash(filePath);

            // Load document using PdfiumViewer (will be implemented in future milestones)
            // For now, we'll set basic properties
            FilePath = filePath;
            PageCount = 1; // Placeholder - will be determined by actual PDF loading
            IsPasswordProtected = false; // Placeholder - will be determined by actual PDF loading
            IsEncrypted = false; // Placeholder - will be determined by actual PDF loading

            return true;
        }
        catch (Exception ex) when (ex is not ValidationException)
        {
            // Log the error securely and rethrow as a generic error
            throw new InvalidOperationException($"Failed to load PDF document: {ex.Message}", ex);
        }
    }

    /// <summary>
    /// Calculates SHA-256 hash of the document for integrity verification.
    /// </summary>
    /// <param name="filePath">Path to the file to hash.</param>
    /// <returns>Hexadecimal string representation of the hash.</returns>
    private static string CalculateDocumentHash(string filePath)
    {
        using var sha256 = SHA256.Create();
        using var stream = File.OpenRead(filePath);
        var hash = sha256.ComputeHash(stream);
        return Convert.ToHexString(hash).ToUpperInvariant();
    }

    /// <summary>
    /// Validates the document's integrity by comparing current hash with stored hash.
    /// </summary>
    /// <returns>True if the document integrity is valid; otherwise, false.</returns>
    public bool ValidateIntegrity()
    {
        if (string.IsNullOrEmpty(FilePath) || string.IsNullOrEmpty(DocumentHash))
        {
            return false;
        }

        if (!File.Exists(FilePath))
        {
            return false;
        }

        var currentHash = CalculateDocumentHash(FilePath);
        return string.Equals(currentHash, DocumentHash, StringComparison.OrdinalIgnoreCase);
    }

    /// <summary>
    /// Disposes of the PdfDocument and releases any unmanaged resources.
    /// </summary>
    public void Dispose()
    {
        Dispose(true);
        GC.SuppressFinalize(this);
    }

    /// <summary>
    /// Disposes of the PdfDocument and releases any unmanaged resources.
    /// </summary>
    /// <param name="disposing">True if called from Dispose(); false if called from finalizer.</param>
    protected virtual void Dispose(bool disposing)
    {
        if (!_disposed && disposing)
        {
            // Dispose managed resources here
            _validator?.Dispose();
            _disposed = true;
        }
    }
}

/// <summary>
/// Validator for PDF document operations with security-focused validation rules.
/// </summary>
public class PdfDocumentValidator : IDisposable
{
    private bool _disposed;

    /// <summary>
    /// Validates a file path for security concerns.
    /// </summary>
    /// <param name="filePath">The file path to validate.</param>
    /// <returns>True if the path is valid; otherwise, false.</returns>
    public static bool ValidateFilePath(string filePath)
    {
        if (string.IsNullOrWhiteSpace(filePath))
        {
            return false;
        }

        // Check for path traversal attempts
        if (filePath.Contains("..", StringComparison.Ordinal) || 
            filePath.Contains("\\..", StringComparison.Ordinal) || 
            filePath.Contains("/..", StringComparison.Ordinal))
        {
            return false;
        }

        // Check for potentially dangerous file extensions
        var extension = Path.GetExtension(filePath).ToUpperInvariant();
        var dangerousExtensions = new[] { ".EXE", ".BAT", ".CMD", ".COM", ".SCR", ".VBS", ".JS" };
        
        if (dangerousExtensions.Contains(extension))
        {
            return false;
        }

        return true;
    }

    /// <summary>
    /// Disposes of the validator and releases any unmanaged resources.
    /// </summary>
    public void Dispose()
    {
        Dispose(true);
        GC.SuppressFinalize(this);
    }

    /// <summary>
    /// Disposes of the validator and releases any unmanaged resources.
    /// </summary>
    /// <param name="disposing">True if called from Dispose(); false if called from finalizer.</param>
    protected virtual void Dispose(bool disposing)
    {
        if (!_disposed && disposing)
        {
            // Dispose managed resources here
            _disposed = true;
        }
    }
}
