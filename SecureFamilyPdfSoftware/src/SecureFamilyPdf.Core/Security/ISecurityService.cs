namespace SecureFamilyPdf.Core.Security;

/// <summary>
/// Service for PDF security operations including password protection and redaction.
/// </summary>
public interface ISecurityService
{
    /// <summary>
    /// Applies password protection to a PDF file.
    /// </summary>
    /// <param name="inputPath">Path to the input PDF file</param>
    /// <param name="outputPath">Path for the output protected PDF file</param>
    /// <param name="password">The password to protect the PDF with</param>
    /// <returns>Task representing the asynchronous operation</returns>
    Task ApplyPasswordProtectionAsync(string inputPath, string outputPath, string password);

    /// <summary>
    /// Removes password protection from a PDF file.
    /// </summary>
    /// <param name="inputPath">Path to the input protected PDF file</param>
    /// <param name="outputPath">Path for the output unprotected PDF file</param>
    /// <param name="password">The password to unlock the PDF</param>
    /// <returns>Task representing the asynchronous operation</returns>
    Task RemovePasswordAsync(string inputPath, string outputPath, string password);

    /// <summary>
    /// Redacts text from a PDF file by searching for the specified term.
    /// </summary>
    /// <param name="inputPath">Path to the input PDF file</param>
    /// <param name="outputPath">Path for the output redacted PDF file</param>
    /// <param name="searchTerm">The text term to redact</param>
    /// <returns>Task representing the asynchronous operation</returns>
    Task RedactTextAsync(string inputPath, string outputPath, string searchTerm);
}
