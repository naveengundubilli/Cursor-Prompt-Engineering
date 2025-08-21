using FluentAssertions;
using Microsoft.Extensions.Logging;
using Moq;
using SecureFamilyPdf.Core.Security;
using Xunit;

namespace SecureFamilyPdf.Tests.Security;

/// <summary>
/// Unit tests for the PdfSecurityManager class.
/// </summary>
public class PdfSecurityManagerTests
{
    private readonly Mock<ILogger<PdfSecurityManager>> _loggerMock;
    private readonly SecuritySettings _settings;
    private readonly PdfSecurityManager _securityManager;

    public PdfSecurityManagerTests()
    {
        _loggerMock = new Mock<ILogger<PdfSecurityManager>>();
        _settings = new SecuritySettings();
        _securityManager = new PdfSecurityManager(_loggerMock.Object, _settings);
    }

    [Fact]
    public void ValidatePdfFile_WithNonExistentFile_ShouldReturnInvalidResult()
    {
        // Arrange
        var nonExistentPath = @"C:\NonExistent\file.pdf";

        // Act
        var result = _securityManager.ValidatePdfFile(nonExistentPath);

        // Assert
        result.IsValid.Should().BeFalse();
        result.Errors.Should().Contain("File does not exist");
    }

    [Fact]
    public void ValidatePdfFile_WithNonPdfExtension_ShouldReturnInvalidResult()
    {
        // Arrange
        var textFilePath = Path.GetTempFileName();

        try
        {
            // Act
            var result = _securityManager.ValidatePdfFile(textFilePath);

            // Assert
            result.IsValid.Should().BeFalse();
            result.Errors.Should().Contain("File is not a PDF");
        }
        finally
        {
            // Cleanup
            if (File.Exists(textFilePath))
            {
                File.Delete(textFilePath);
            }
        }
    }

    [Fact]
    public void ValidatePdfFile_WithValidPdfHeader_ShouldReturnValidResult()
    {
        // Arrange
        var pdfFilePath = CreateTestPdfFile();

        try
        {
            // Act
            var result = _securityManager.ValidatePdfFile(pdfFilePath);

            // Assert
            result.IsValid.Should().BeTrue();
            result.Errors.Should().BeEmpty();
        }
        finally
        {
            // Cleanup
            if (File.Exists(pdfFilePath))
            {
                File.Delete(pdfFilePath);
            }
        }
    }

    [Fact]
    public void ValidatePdfFile_WithInvalidPdfHeader_ShouldReturnInvalidResult()
    {
        // Arrange
        var invalidPdfPath = Path.Combine(Path.GetTempPath(), "invalid.pdf");
        
        try
        {
            // Create a file with invalid PDF header
            File.WriteAllText(invalidPdfPath, "This is not a PDF file");

            // Act
            var result = _securityManager.ValidatePdfFile(invalidPdfPath);

            // Assert
            result.IsValid.Should().BeFalse();
            result.Errors.Should().Contain("Invalid PDF file format");
        }
        finally
        {
            // Cleanup
            if (File.Exists(invalidPdfPath))
            {
                File.Delete(invalidPdfPath);
            }
        }
    }

    [Fact]
    public void ValidatePdfFile_WithLargeFile_ShouldReturnInvalidResult()
    {
        // Arrange
        _settings.MaxFileSizeBytes = 1024; // 1KB limit
        var largePdfPath = CreateLargeTestFile();

        try
        {
            // Act
            var result = _securityManager.ValidatePdfFile(largePdfPath);

            // Assert
            result.IsValid.Should().BeFalse();
            result.Errors.Should().ContainMatch("*exceeds maximum allowed size*");
        }
        finally
        {
            // Cleanup
            if (File.Exists(largePdfPath))
            {
                File.Delete(largePdfPath);
            }
        }
    }

    [Fact]
    public void SecuritySettings_DefaultValues_ShouldBeSecure()
    {
        // Arrange & Act
        var settings = new SecuritySettings();

        // Assert
        settings.DisableJavaScript.Should().BeTrue();
        settings.DisableExternalLinks.Should().BeTrue();
        settings.DisableEmbeddedFiles.Should().BeTrue();
        settings.DisableFormSubmission.Should().BeTrue();
        settings.EnableRedaction.Should().BeTrue();
        settings.RequirePasswordForSensitiveOperations.Should().BeTrue();
        settings.MaxFileSizeBytes.Should().Be(100 * 1024 * 1024); // 100MB
    }

    [Fact]
    public void PdfValidationResult_AddError_ShouldAddErrorToList()
    {
        // Arrange
        var result = new PdfValidationResult();
        var errorMessage = "Test error message";

        // Act
        result.AddError(errorMessage);

        // Assert
        result.Errors.Should().ContainSingle();
        result.Errors[0].Should().Be(errorMessage);
    }

    [Fact]
    public void PdfValidationResult_AddErrors_ShouldAddMultipleErrors()
    {
        // Arrange
        var result = new PdfValidationResult();
        var errors = new[] { "Error 1", "Error 2", "Error 3" };

        // Act
        result.AddErrors(errors);

        // Assert
        result.Errors.Should().HaveCount(3);
        result.Errors.Should().Contain(errors);
    }

    [Fact]
    public void PdfValidationResult_AddErrorsWithNull_ShouldThrowArgumentNullException()
    {
        // Arrange
        var result = new PdfValidationResult();

        // Act & Assert
        var action = () => result.AddErrors(null!);
        action.Should().Throw<ArgumentNullException>();
    }

    [Fact]
    public void PdfValidationResult_AddErrorWithEmptyString_ShouldThrowArgumentException()
    {
        // Arrange
        var result = new PdfValidationResult();

        // Act & Assert
        var action = () => result.AddError("");
        action.Should().Throw<ArgumentException>();
    }

    /// <summary>
    /// Creates a test PDF file with valid PDF header.
    /// </summary>
    private static string CreateTestPdfFile()
    {
        var pdfPath = Path.Combine(Path.GetTempPath(), $"test_{Guid.NewGuid()}.pdf");
        
        // Create a minimal valid PDF file
        using var writer = new BinaryWriter(File.Create(pdfPath));
        
        // PDF header
        var header = System.Text.Encoding.ASCII.GetBytes("%PDF-1.4\n");
        writer.Write(header);
        
        // Minimal PDF content (simplified)
        var content = System.Text.Encoding.ASCII.GetBytes("1 0 obj\n<<\n/Type /Catalog\n/Pages 2 0 R\n>>\nendobj\n");
        writer.Write(content);
        
        // PDF trailer
        var trailer = System.Text.Encoding.ASCII.GetBytes("xref\n0 3\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \ntrailer\n<<\n/Size 3\n/Root 1 0 R\n>>\nstartxref\n108\n%%EOF\n");
        writer.Write(trailer);
        
        return pdfPath;
    }

    /// <summary>
    /// Creates a large test file for size validation testing.
    /// </summary>
    private static string CreateLargeTestFile()
    {
        var largePath = Path.Combine(Path.GetTempPath(), $"large_{Guid.NewGuid()}.pdf");
        
        // Create a file larger than the limit
        using var writer = new BinaryWriter(File.Create(largePath));
        
        // Write PDF header
        var header = System.Text.Encoding.ASCII.GetBytes("%PDF-1.4\n");
        writer.Write(header);
        
        // Write large amount of data to exceed the limit
        var largeData = new byte[2048]; // 2KB
        for (int i = 0; i < 1000; i++) // Write 2MB total
        {
            writer.Write(largeData);
        }
        
        return largePath;
    }
}
