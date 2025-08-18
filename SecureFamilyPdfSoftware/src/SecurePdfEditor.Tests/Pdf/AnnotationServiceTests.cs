using System;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using FluentAssertions;
using SecurePdfEditor.Core.Pdf;
using SecurePdfEditor.Core.Pdf.Models;
using Xunit;

namespace SecurePdfEditor.Tests.Pdf;

/// <summary>
/// Unit tests for the AnnotationService class to ensure proper PDF annotation functionality.
/// </summary>
public class AnnotationServiceTests : IDisposable
{
	private readonly string _testFilePath;
	private readonly string _testDirectory;
	private readonly string _outputDirectory;
	private bool _disposed;

	public AnnotationServiceTests()
	{
		// Create temporary test directories
		_testDirectory = Path.Combine(Path.GetTempPath(), "SecurePdfEditorAnnotationTests");
		_outputDirectory = Path.Combine(Path.GetTempPath(), "SecurePdfEditorAnnotationOutput");
		Directory.CreateDirectory(_testDirectory);
		Directory.CreateDirectory(_outputDirectory);
		
		// Create a test PDF file (simulated)
		_testFilePath = Path.Combine(_testDirectory, "test.pdf");
		CreateTestPdfFile();
	}

	[Fact]
	public void ConstructorShouldCreateValidInstance()
	{
		// Act
		using var service = new AnnotationService();

		// Assert
		service.Should().NotBeNull();
		service.IsLoaded.Should().BeFalse();
		service.FilePath.Should().BeNull();
		service.TotalPages.Should().Be(0);
		service.TextHighlights.Should().BeEmpty();
		service.StickyNotes.Should().BeEmpty();
	}

	[Fact]
	public void LoadPdfWithValidFilePathShouldLoadSuccessfully()
	{
		// Arrange
		using var service = new AnnotationService();

		// Act
		var result = service.LoadPdf(_testFilePath);

		// Assert
		result.Should().BeTrue();
		service.IsLoaded.Should().BeTrue();
		service.FilePath.Should().Be(_testFilePath);
		service.TotalPages.Should().BeGreaterThan(0);
	}

	[Fact]
	public void LoadPdfWithNullFilePathShouldThrowArgumentException()
	{
		// Arrange
		using var service = new AnnotationService();

		// Act & Assert
		var action = () => service.LoadPdf(null!);
		action.Should().Throw<ArgumentException>()
			  .WithParameterName("filePath");
	}

	[Fact]
	public void LoadPdfWithEmptyFilePathShouldThrowArgumentException()
	{
		// Arrange
		using var service = new AnnotationService();

		// Act & Assert
		var action = () => service.LoadPdf("");
		action.Should().Throw<ArgumentException>()
			  .WithParameterName("filePath");
	}

	[Fact]
	public void LoadPdfWithNonExistentFileShouldThrowFileNotFoundException()
	{
		// Arrange
		using var service = new AnnotationService();
		var nonExistentPath = Path.Combine(_testDirectory, "nonexistent.pdf");

		// Act & Assert
		var action = () => service.LoadPdf(nonExistentPath);
		action.Should().Throw<FileNotFoundException>();
	}

	[Fact]
	public void LoadPdfWithInvalidFilePathShouldThrowArgumentException()
	{
		// Arrange
		using var service = new AnnotationService();
		var invalidPath = Path.Combine(_testDirectory, "test.exe");
		File.WriteAllText(invalidPath, "This is not a PDF file");

		// Act & Assert
		var action = () => service.LoadPdf(invalidPath);
		action.Should().Throw<ArgumentException>()
			  .WithMessage("*Invalid or potentially dangerous file path*");
	}

	[Fact]
	public void HighlightTextWithValidRangeShouldCreateAnnotation()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var range = new TextRange(1, 0, 10);
		var color = Color.Yellow;

		// Act
		var annotation = service.HighlightText(range, color);

		// Assert
		annotation.Should().NotBeNull();
		annotation.Range.Should().Be(range);
		annotation.Color.Should().Be(color);
		service.TextHighlights.Should().HaveCount(1);
		service.TextHighlights[0].Should().Be(annotation);
	}

	[Fact]
	public void HighlightTextWithoutLoadedPdfShouldThrowInvalidOperationException()
	{
		// Arrange
		using var service = new AnnotationService();
		var range = new TextRange(1, 0, 10);
		var color = Color.Yellow;

		// Act & Assert
		var action = () => service.HighlightText(range, color);
		action.Should().Throw<InvalidOperationException>()
			  .WithMessage("*No PDF is loaded for annotation*");
	}

	[Fact]
	public void HighlightTextWithInvalidPageNumberShouldThrowArgumentOutOfRangeException()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var range = new TextRange(0, 0, 10); // Invalid page number
		var color = Color.Yellow;

		// Act & Assert
		var action = () => service.HighlightText(range, color);
		action.Should().Throw<ArgumentOutOfRangeException>()
			  .WithParameterName("range");
	}

	[Fact]
	public void HighlightTextWithOverlappingRangeShouldThrowInvalidOperationException()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var range1 = new TextRange(1, 0, 10);
		var range2 = new TextRange(1, 5, 15); // Overlaps with range1
		var color = Color.Yellow;

		// Act
		service.HighlightText(range1, color);

		// Assert
		var action = () => service.HighlightText(range2, color);
		action.Should().Throw<InvalidOperationException>()
			  .WithMessage("*Text range overlaps with existing highlight*");
	}

	[Fact]
	public void AddStickyNoteWithValidPositionShouldCreateAnnotation()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var position = new AnnotationPosition(1, 100, 200);
		var text = "Test sticky note";

		// Act
		var annotation = service.AddStickyNote(position, text);

		// Assert
		annotation.Should().NotBeNull();
		annotation.Position.Should().Be(position);
		annotation.Text.Should().Be(text);
		service.StickyNotes.Should().HaveCount(1);
		service.StickyNotes[0].Should().Be(annotation);
	}

	[Fact]
	public void AddStickyNoteWithoutLoadedPdfShouldThrowInvalidOperationException()
	{
		// Arrange
		using var service = new AnnotationService();
		var position = new AnnotationPosition(1, 100, 200);
		var text = "Test sticky note";

		// Act & Assert
		var action = () => service.AddStickyNote(position, text);
		action.Should().Throw<InvalidOperationException>()
			  .WithMessage("*No PDF is loaded for annotation*");
	}

	[Fact]
	public void AddStickyNoteWithNullTextShouldThrowArgumentException()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var position = new AnnotationPosition(1, 100, 200);

		// Act & Assert
		var action = () => service.AddStickyNote(position, null!);
		action.Should().Throw<ArgumentException>()
			  .WithParameterName("text");
	}

	[Fact]
	public void AddStickyNoteWithEmptyTextShouldThrowArgumentException()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var position = new AnnotationPosition(1, 100, 200);

		// Act & Assert
		var action = () => service.AddStickyNote(position, "");
		action.Should().Throw<ArgumentException>()
			  .WithParameterName("text");
	}

	[Fact]
	public void AddStickyNoteWithInvalidPageNumberShouldThrowArgumentOutOfRangeException()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var position = new AnnotationPosition(0, 100, 200); // Invalid page number
		var text = "Test sticky note";

		// Act & Assert
		var action = () => service.AddStickyNote(position, text);
		action.Should().Throw<ArgumentOutOfRangeException>()
			  .WithParameterName("position");
	}

	[Fact]
	public void RemoveTextHighlightWithValidIdShouldRemoveAnnotation()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var range = new TextRange(1, 0, 10);
		var color = Color.Yellow;
		var annotation = service.HighlightText(range, color);

		// Act
		var result = service.RemoveTextHighlight(annotation.Id);

		// Assert
		result.Should().BeTrue();
		service.TextHighlights.Should().BeEmpty();
	}

	[Fact]
	public void RemoveTextHighlightWithInvalidIdShouldReturnFalse()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);

		// Act
		var result = service.RemoveTextHighlight(Guid.NewGuid());

		// Assert
		result.Should().BeFalse();
	}

	[Fact]
	public void RemoveStickyNoteWithValidIdShouldRemoveAnnotation()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var position = new AnnotationPosition(1, 100, 200);
		var text = "Test sticky note";
		var annotation = service.AddStickyNote(position, text);

		// Act
		var result = service.RemoveStickyNote(annotation.Id);

		// Assert
		result.Should().BeTrue();
		service.StickyNotes.Should().BeEmpty();
	}

	[Fact]
	public void RemoveStickyNoteWithInvalidIdShouldReturnFalse()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);

		// Act
		var result = service.RemoveStickyNote(Guid.NewGuid());

		// Assert
		result.Should().BeFalse();
	}

	[Fact]
	public void SaveAnnotationsWithValidPathShouldCreatePdf()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var range = new TextRange(1, 0, 10);
		var color = Color.Yellow;
		service.HighlightText(range, color);
		
		var position = new AnnotationPosition(1, 100, 200);
		var text = "Test sticky note";
		service.AddStickyNote(position, text);
		
		var outputPath = Path.Combine(_outputDirectory, "annotated.pdf");

		// Act
		var result = service.SaveAnnotations(outputPath);

		// Assert
		result.Should().BeTrue();
		File.Exists(outputPath).Should().BeTrue();
	}

	[Fact]
	public void SaveAnnotationsWithoutLoadedPdfShouldThrowInvalidOperationException()
	{
		// Arrange
		using var service = new AnnotationService();
		var outputPath = Path.Combine(_outputDirectory, "annotated.pdf");

		// Act & Assert
		var action = () => service.SaveAnnotations(outputPath);
		action.Should().Throw<InvalidOperationException>()
			  .WithMessage("*No PDF is loaded for annotation*");
	}

	[Fact]
	public void SaveAnnotationsWithNullPathShouldThrowArgumentException()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);

		// Act & Assert
		var action = () => service.SaveAnnotations(null!);
		action.Should().Throw<ArgumentException>()
			  .WithParameterName("outputPath");
	}

	[Fact]
	public void SaveAnnotationsWithEmptyPathShouldThrowArgumentException()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);

		// Act & Assert
		var action = () => service.SaveAnnotations("");
		action.Should().Throw<ArgumentException>()
			  .WithParameterName("outputPath");
	}

	[Fact]
	public async Task SaveAnnotationsAsyncShouldCreatePdf()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var range = new TextRange(1, 0, 10);
		var color = Color.Yellow;
		service.HighlightText(range, color);
		
		var outputPath = Path.Combine(_outputDirectory, "annotated_async.pdf");

		// Act
		var result = await service.SaveAnnotationsAsync(outputPath).ConfigureAwait(true);

		// Assert
		result.Should().BeTrue();
		File.Exists(outputPath).Should().BeTrue();
	}

	[Fact]
	public void ClearAnnotationsShouldRemoveAllAnnotations()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var range = new TextRange(1, 0, 10);
		var color = Color.Yellow;
		service.HighlightText(range, color);
		
		var position = new AnnotationPosition(1, 100, 200);
		var text = "Test sticky note";
		service.AddStickyNote(position, text);

		// Act
		service.ClearAnnotations();

		// Assert
		service.TextHighlights.Should().BeEmpty();
		service.StickyNotes.Should().BeEmpty();
	}

	[Fact]
	public void UnloadPdfShouldClearAllState()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);
		var range = new TextRange(1, 0, 10);
		var color = Color.Yellow;
		service.HighlightText(range, color);

		// Act
		service.UnloadPdf();

		// Assert
		service.IsLoaded.Should().BeFalse();
		service.FilePath.Should().BeNull();
		service.TotalPages.Should().Be(0);
		service.TextHighlights.Should().BeEmpty();
		service.StickyNotes.Should().BeEmpty();
	}

	[Fact]
	public void DisposeShouldNotThrowException()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);

		// Act & Assert
		var action = () => service.Dispose();
		action.Should().NotThrow();
	}

	[Fact]
	public void DisposeShouldReleaseResources()
	{
		// Arrange
		using var service = new AnnotationService();
		service.LoadPdf(_testFilePath);

		// Act
		service.Dispose();

		// Assert
		service.IsLoaded.Should().BeFalse();
	}

	[Fact]
	public void TextRangeConstructorWithValidParametersShouldCreateValidRange()
	{
		// Act
		var range = new TextRange(1, 0, 10);

		// Assert
		range.PageNumber.Should().Be(1);
		range.StartIndex.Should().Be(0);
		range.EndIndex.Should().Be(10);
		range.Length.Should().Be(10);
	}

	[Fact]
	public void TextRangeConstructorWithInvalidPageNumberShouldThrowArgumentOutOfRangeException()
	{
		// Act & Assert
		var action = () => new TextRange(0, 0, 10);
		action.Should().Throw<ArgumentOutOfRangeException>()
			  .WithParameterName("pageNumber");
	}

	[Fact]
	public void TextRangeConstructorWithInvalidStartIndexShouldThrowArgumentOutOfRangeException()
	{
		// Act & Assert
		var action = () => new TextRange(1, -1, 10);
		action.Should().Throw<ArgumentOutOfRangeException>()
			  .WithParameterName("startIndex");
	}

	[Fact]
	public void TextRangeConstructorWithInvalidEndIndexShouldThrowArgumentOutOfRangeException()
	{
		// Act & Assert
		var action = () => new TextRange(1, 10, 5);
		action.Should().Throw<ArgumentOutOfRangeException>()
			  .WithParameterName("endIndex");
	}

	[Fact]
	public void TextRangeOverlapsShouldReturnTrueForOverlappingRanges()
	{
		// Arrange
		var range1 = new TextRange(1, 0, 10);
		var range2 = new TextRange(1, 5, 15);

		// Act
		var result = range1.Overlaps(range2);

		// Assert
		result.Should().BeTrue();
	}

	[Fact]
	public void TextRangeOverlapsShouldReturnFalseForNonOverlappingRanges()
	{
		// Arrange
		var range1 = new TextRange(1, 0, 10);
		var range2 = new TextRange(1, 15, 25);

		// Act
		var result = range1.Overlaps(range2);

		// Assert
		result.Should().BeFalse();
	}

	[Fact]
	public void TextRangeOverlapsShouldReturnFalseForDifferentPages()
	{
		// Arrange
		var range1 = new TextRange(1, 0, 10);
		var range2 = new TextRange(2, 0, 10);

		// Act
		var result = range1.Overlaps(range2);

		// Assert
		result.Should().BeFalse();
	}

	[Fact]
	public void AnnotationPositionConstructorWithValidParametersShouldCreateValidPosition()
	{
		// Act
		var position = new AnnotationPosition(1, 100.5f, 200.5f);

		// Assert
		position.PageNumber.Should().Be(1);
		position.X.Should().Be(100.5f);
		position.Y.Should().Be(200.5f);
	}

	[Fact]
	public void AnnotationPositionConstructorWithInvalidPageNumberShouldThrowArgumentOutOfRangeException()
	{
		// Act & Assert
		var action = () => new AnnotationPosition(0, 100, 200);
		action.Should().Throw<ArgumentOutOfRangeException>()
			  .WithParameterName("pageNumber");
	}

	/// <summary>
	/// Creates a test PDF file for testing purposes.
	/// In a real implementation, this would create a valid PDF file.
	/// </summary>
	private void CreateTestPdfFile()
	{
		// Create a minimal PDF file content for testing
		// This is a simplified PDF structure - in production, you'd use a proper PDF library
		var pdfContent = @"%PDF-1.4
1 0 obj
<<
/Type /Catalog
/Pages 2 0 R
>>
endobj

2 0 obj
<<
/Type /Pages
/Kids [3 0 R 4 0 R]
/Count 2
>>
endobj

3 0 obj
<<
/Type /Page
/Parent 2 0 R
/MediaBox [0 0 612 792]
/Contents 5 0 R
>>
endobj

4 0 obj
<<
/Type /Page
/Parent 2 0 R
/MediaBox [0 0 612 792]
/Contents 6 0 R
>>
endobj

5 0 obj
<<
/Length 44
>>
stream
BT
/F1 12 Tf
72 720 Td
(Page 1) Tj
ET
endstream
endobj

6 0 obj
<<
/Length 44
>>
stream
BT
/F1 12 Tf
72 720 Td
(Page 2) Tj
ET
endstream
endobj

xref
0 7
0000000000 65535 f 
0000000009 00000 n 
0000000058 00000 n 
0000000115 00000 n 
0000000172 00000 n 
0000000229 00000 n 
0000000318 00000 n 
trailer
<<
/Size 7
/Root 1 0 R
>>
startxref
397
%%EOF";

		File.WriteAllText(_testFilePath, pdfContent);
	}

	public void Dispose()
	{
		Dispose(true);
		GC.SuppressFinalize(this);
	}

	protected virtual void Dispose(bool disposing)
	{
		if (!_disposed && disposing)
		{
			// Clean up test files
			try
			{
				if (File.Exists(_testFilePath))
				{
					File.Delete(_testFilePath);
				}

				if (Directory.Exists(_testDirectory))
				{
					Directory.Delete(_testDirectory, true);
				}

				if (Directory.Exists(_outputDirectory))
				{
					Directory.Delete(_outputDirectory, true);
				}
			}
			catch (System.IO.IOException)
			{
				// Ignore cleanup errors in tests
			}
			catch (System.UnauthorizedAccessException)
			{
				// Ignore cleanup errors in tests
			}
			_disposed = true;
		}
	}
}
