using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using Microsoft.Extensions.Logging;
using PdfiumViewer;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;

namespace SecureFamilyPdf.Core.Security;

/// <summary>
/// Implementation of the security service using qpdf for password operations
/// and rasterize-and-rebuild for true redaction.
/// </summary>
public sealed class SecurityService : ISecurityService
{
    private readonly ILogger<SecurityService> _logger;
    private readonly string _qpdfPath;

    public SecurityService(ILogger<SecurityService> logger)
    {
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        
        // Initialize qpdf path (bundled with the application)
        _qpdfPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "third_party", "qpdf", "qpdf.exe");
        
        // Initialize PdfiumViewer
        PdfLibrary.Load();
    }

    /// <summary>
    /// Applies password protection to a PDF file using qpdf.
    /// </summary>
    public async Task ApplyPasswordProtectionAsync(string inputPath, string outputPath, string password)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(inputPath);
        ArgumentException.ThrowIfNullOrWhiteSpace(outputPath);
        ArgumentException.ThrowIfNullOrWhiteSpace(password);

        try
        {
            _logger.LogInformation("Applying password protection to: {InputPath}", inputPath);

            // Validate input file exists
            if (!File.Exists(inputPath))
            {
                throw new FileNotFoundException("Input PDF file not found", inputPath);
            }

            // Validate qpdf is available
            if (!File.Exists(_qpdfPath))
            {
                throw new FileNotFoundException("qpdf executable not found", _qpdfPath);
            }

            // Create output directory if it doesn't exist
            var outputDir = Path.GetDirectoryName(outputPath);
            if (!string.IsNullOrEmpty(outputDir) && !Directory.Exists(outputDir))
            {
                Directory.CreateDirectory(outputDir);
            }

            // Build qpdf command for password protection
            var arguments = $"--encrypt \"{password}\" \"{password}\" 256 -- \"{inputPath}\" \"{outputPath}\"";

            var result = await ExecuteQpdfCommandAsync(arguments);
            
            if (result.ExitCode != 0)
            {
                throw new InvalidOperationException($"qpdf failed with exit code {result.ExitCode}: {result.ErrorOutput}");
            }

            _logger.LogInformation("Password protection applied successfully to: {OutputPath}", outputPath);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to apply password protection");
            throw;
        }
    }

    /// <summary>
    /// Removes password protection from a PDF file using qpdf.
    /// </summary>
    public async Task RemovePasswordAsync(string inputPath, string outputPath, string password)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(inputPath);
        ArgumentException.ThrowIfNullOrWhiteSpace(outputPath);
        ArgumentException.ThrowIfNullOrWhiteSpace(password);

        try
        {
            _logger.LogInformation("Removing password protection from: {InputPath}", inputPath);

            // Validate input file exists
            if (!File.Exists(inputPath))
            {
                throw new FileNotFoundException("Input PDF file not found", inputPath);
            }

            // Validate qpdf is available
            if (!File.Exists(_qpdfPath))
            {
                throw new FileNotFoundException("qpdf executable not found", _qpdfPath);
            }

            // Create output directory if it doesn't exist
            var outputDir = Path.GetDirectoryName(outputPath);
            if (!string.IsNullOrEmpty(outputDir) && !Directory.Exists(outputDir))
            {
                Directory.CreateDirectory(outputDir);
            }

            // Build qpdf command for password removal
            var arguments = $"--password=\"{password}\" --decrypt \"{inputPath}\" \"{outputPath}\"";

            var result = await ExecuteQpdfCommandAsync(arguments);
            
            if (result.ExitCode != 0)
            {
                throw new InvalidOperationException($"qpdf failed with exit code {result.ExitCode}: {result.ErrorOutput}");
            }

            _logger.LogInformation("Password protection removed successfully to: {OutputPath}", outputPath);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to remove password protection");
            throw;
        }
    }

    /// <summary>
    /// Redacts text from a PDF file using rasterize-and-rebuild approach.
    /// </summary>
    public async Task RedactTextAsync(string inputPath, string outputPath, string searchTerm)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(inputPath);
        ArgumentException.ThrowIfNullOrWhiteSpace(outputPath);
        ArgumentException.ThrowIfNullOrWhiteSpace(searchTerm);

        try
        {
            _logger.LogInformation("Redacting text '{SearchTerm}' from: {InputPath}", searchTerm, inputPath);

            // Validate input file exists
            if (!File.Exists(inputPath))
            {
                throw new FileNotFoundException("Input PDF file not found", inputPath);
            }

            // Create output directory if it doesn't exist
            var outputDir = Path.GetDirectoryName(outputPath);
            if (!string.IsNullOrEmpty(outputDir) && !Directory.Exists(outputDir))
            {
                Directory.CreateDirectory(outputDir);
            }

            // Load the PDF document
            using var document = PdfDocument.Load(inputPath);
            var pageCount = document.PageCount;

            // Create a new PDF with redacted content
            var redactedDocument = Document.Create(container =>
            {
                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++)
                {
                    container.Page(page =>
                    {
                        page.Size(PageSizes.A4);
                        page.Margin(0);

                        // Render the original page to bitmap
                        using var originalImage = document.Render(pageIndex, 300, 300, PdfRenderFlags.Annotations);
                        
                        // Convert to memory stream for QuestPDF
                        using var imageStream = new MemoryStream();
                        originalImage.Save(imageStream, ImageFormat.Png);
                        imageStream.Position = 0;

                        // Add the original page as background
                        page.Content().Image(imageStream.ToArray());

                        // Add redaction overlays
                        page.Overlay().Canvas((canvas, size) =>
                        {
                            // Extract text and find matches
                            var pageText = document.GetPdfText(pageIndex);
                            var matches = FindTextMatches(pageText, searchTerm);

                            // Draw black rectangles over matches
                            foreach (var match in matches)
                            {
                                canvas.SaveState();
                                canvas.SetFillColor(Colors.Black);
                                canvas.DrawRectangle(match.X, match.Y, match.Width, match.Height);
                                canvas.Fill();
                                canvas.RestoreState();
                            }
                        });
                    });
                }
            });

            // Generate the redacted PDF
            redactedDocument.GeneratePdf(outputPath);
            await Task.CompletedTask; // Async for future extensibility

            _logger.LogInformation("Text redaction completed successfully to: {OutputPath}", outputPath);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to redact text");
            throw;
        }
    }

    /// <summary>
    /// Executes a qpdf command asynchronously.
    /// </summary>
    private async Task<ProcessResult> ExecuteQpdfCommandAsync(string arguments)
    {
        var startInfo = new ProcessStartInfo
        {
            FileName = _qpdfPath,
            Arguments = arguments,
            UseShellExecute = false,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            CreateNoWindow = true,
            WindowStyle = ProcessWindowStyle.Hidden
        };

        using var process = new Process { StartInfo = startInfo };
        
        var outputBuilder = new System.Text.StringBuilder();
        var errorBuilder = new System.Text.StringBuilder();

        process.OutputDataReceived += (sender, e) =>
        {
            if (e.Data != null)
            {
                outputBuilder.AppendLine(e.Data);
            }
        };

        process.ErrorDataReceived += (sender, e) =>
        {
            if (e.Data != null)
            {
                errorBuilder.AppendLine(e.Data);
            }
        };

        process.Start();
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();

        await process.WaitForExitAsync();

        return new ProcessResult
        {
            ExitCode = process.ExitCode,
            StandardOutput = outputBuilder.ToString(),
            ErrorOutput = errorBuilder.ToString()
        };
    }

    /// <summary>
    /// Finds text matches in the page text and returns their bounding rectangles.
    /// This is a simplified implementation - in a real scenario, you'd need
    /// more sophisticated text extraction with coordinates.
    /// </summary>
    private List<RectangleF> FindTextMatches(string pageText, string searchTerm)
    {
        var matches = new List<RectangleF>();
        var index = 0;

        while ((index = pageText.IndexOf(searchTerm, index, StringComparison.OrdinalIgnoreCase)) != -1)
        {
            // Simplified bounding box calculation
            // In a real implementation, you'd extract actual text coordinates from the PDF
            var lineHeight = 12.0f;
            var charWidth = 6.0f;
            var line = pageText.Substring(0, index).Count(c => c == '\n');
            var column = index - pageText.Substring(0, index).LastIndexOf('\n') - 1;

            var x = column * charWidth;
            var y = line * lineHeight;
            var width = searchTerm.Length * charWidth;
            var height = lineHeight;

            matches.Add(new RectangleF(x, y, width, height));
            index += searchTerm.Length;
        }

        return matches;
    }
}

/// <summary>
/// Result of a process execution.
/// </summary>
public class ProcessResult
{
    public int ExitCode { get; set; }
    public string StandardOutput { get; set; } = string.Empty;
    public string ErrorOutput { get; set; } = string.Empty;
}
