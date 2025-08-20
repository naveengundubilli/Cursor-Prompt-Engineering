package com.securepdfeditor.creation;

import com.securepdfeditor.security.CryptoService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreationService
 */
class CreationServiceTest {
    
    private CreationService creationService;
    
    @BeforeEach
    void setUp() {
        creationService = new CreationService(null); // CryptoService has static methods
    }
    
    @Test
    void testCreateBlankDocument() throws IOException {
        // Test creating a blank document with A4 size
        PDDocument document = creationService.createBlankDocument(
            CreationService.PageSize.A4,
            CreationService.Orientation.PORTRAIT,
            3
        );
        
        assertNotNull(document);
        assertEquals(3, document.getNumberOfPages());
        
        // Check page size
        PDPage firstPage = document.getPage(0);
        PDRectangle mediaBox = firstPage.getMediaBox();
        assertEquals(PDRectangle.A4.getWidth(), mediaBox.getWidth(), 0.1);
        assertEquals(PDRectangle.A4.getHeight(), mediaBox.getHeight(), 0.1);
        
        document.close();
    }
    
    @Test
    void testCreateBlankDocumentLandscape() throws IOException {
        // Test creating a landscape document
        PDDocument document = creationService.createBlankDocument(
            CreationService.PageSize.LETTER,
            CreationService.Orientation.LANDSCAPE,
            1
        );
        
        assertNotNull(document);
        assertEquals(1, document.getNumberOfPages());
        
        // Check page size (should be rotated)
        PDPage firstPage = document.getPage(0);
        PDRectangle mediaBox = firstPage.getMediaBox();
        assertEquals(PDRectangle.LETTER.getHeight(), mediaBox.getWidth(), 0.1);
        assertEquals(PDRectangle.LETTER.getWidth(), mediaBox.getHeight(), 0.1);
        
        document.close();
    }
    
    @Test
    void testCreateFromTemplate() throws IOException {
        // Test creating from template
        PDDocument document = creationService.createFromTemplate("Blank", 2);
        
        assertNotNull(document);
        assertEquals(2, document.getNumberOfPages());
        
        document.close();
    }
    
    @Test
    void testCreateFromInvalidTemplate() {
        // Test creating from non-existent template
        assertThrows(IllegalArgumentException.class, () -> {
            creationService.createFromTemplate("NonExistentTemplate", 1);
        });
    }
    
    @Test
    void testSaveDocument(@TempDir Path tempDir) throws IOException {
        // Test saving a document
        PDDocument document = creationService.createBlankDocument(
            CreationService.PageSize.A4,
            CreationService.Orientation.PORTRAIT,
            1
        );
        
        Path filePath = tempDir.resolve("test-document.pdf");
        creationService.saveDocument(document, filePath, null);
        
        assertTrue(Files.exists(filePath));
        assertTrue(Files.size(filePath) > 0);
        
        document.close();
    }
    
    @Test
    void testGetAvailablePageSizes() {
        List<CreationService.PageSize> pageSizes = creationService.getAvailablePageSizes();
        
        assertNotNull(pageSizes);
        assertFalse(pageSizes.isEmpty());
        
        // Check that all expected page sizes are present
        assertTrue(pageSizes.contains(CreationService.PageSize.A4));
        assertTrue(pageSizes.contains(CreationService.PageSize.LETTER));
        assertTrue(pageSizes.contains(CreationService.PageSize.LEGAL));
        assertTrue(pageSizes.contains(CreationService.PageSize.A3));
        assertTrue(pageSizes.contains(CreationService.PageSize.A5));
    }
    
    @Test
    void testGetAvailableTemplates() {
        List<String> templates = creationService.getAvailableTemplates();
        
        assertNotNull(templates);
        assertFalse(templates.isEmpty());
        
        // Check that all expected templates are present
        assertTrue(templates.contains("Blank"));
        assertTrue(templates.contains("Lined Page"));
        assertTrue(templates.contains("Grid Page"));
        assertTrue(templates.contains("Title Page"));
        assertTrue(templates.contains("Meeting Notes"));
    }
    
    @Test
    void testGetAvailableTextStyles() {
        List<String> styles = creationService.getAvailableTextStyles();
        
        assertNotNull(styles);
        assertFalse(styles.isEmpty());
        
        // Check that all expected styles are present
        assertTrue(styles.contains("Heading 1"));
        assertTrue(styles.contains("Heading 2"));
        assertTrue(styles.contains("Heading 3"));
        assertTrue(styles.contains("Body"));
        assertTrue(styles.contains("Caption"));
        assertTrue(styles.contains("Title"));
    }
    
    @Test
    void testAddAndGetTextStyle() {
        // Test adding a custom text style
        TextStyle customStyle = new TextStyle(
            new org.apache.pdfbox.pdmodel.font.PDType1Font(
                org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD
            ),
            16,
            java.awt.Color.BLUE,
            CreationService.TextAlignment.CENTER
        );
        
        creationService.addTextStyle("Custom Style", customStyle);
        
        // Verify the style was added
        List<String> styles = creationService.getAvailableTextStyles();
        assertTrue(styles.contains("Custom Style"));
        
        // Verify we can retrieve the style
        TextStyle retrievedStyle = creationService.getTextStyle("Custom Style");
        assertNotNull(retrievedStyle);
        assertEquals(16, retrievedStyle.getFontSize());
        assertEquals(java.awt.Color.BLUE, retrievedStyle.getColor());
        assertEquals(CreationService.TextAlignment.CENTER, retrievedStyle.getAlignment());
    }
    
    @Test
    void testGetTextStyleNotFound() {
        // Test getting a non-existent style
        TextStyle style = creationService.getTextStyle("NonExistentStyle");
        assertNull(style);
    }
    
    @Test
    void testPageSizeProperties() {
        // Test page size properties
        CreationService.PageSize a4 = CreationService.PageSize.A4;
        
        assertEquals("A4", a4.getName());
        assertEquals(PDRectangle.A4, a4.getRectangle());
        assertEquals(595, a4.getWidth(), 0.1);
        assertEquals(842, a4.getHeight(), 0.1);
    }
    
    @Test
    void testOrientationProperties() {
        // Test orientation properties
        CreationService.Orientation portrait = CreationService.Orientation.PORTRAIT;
        CreationService.Orientation landscape = CreationService.Orientation.LANDSCAPE;
        
        assertEquals("Portrait", portrait.getName());
        assertEquals("Landscape", landscape.getName());
    }
    
    @Test
    void testTextStyleProperties() {
        // Test text style properties
        TextStyle style = new TextStyle(
            new org.apache.pdfbox.pdmodel.font.PDType1Font(
                org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA
            ),
            14,
            java.awt.Color.BLACK,
            CreationService.TextAlignment.LEFT,
            1.5f,
            10f,
            5f
        );
        
        assertEquals(14, style.getFontSize());
        assertEquals(java.awt.Color.BLACK, style.getColor());
        assertEquals(CreationService.TextAlignment.LEFT, style.getAlignment());
        assertEquals(1.5f, style.getLineSpacing());
        assertEquals(10f, style.getSpacingBefore());
        assertEquals(5f, style.getSpacingAfter());
    }
    
    @Test
    void testTextStyleWithModifications() {
        // Test text style modification methods
        TextStyle originalStyle = new TextStyle(
            new org.apache.pdfbox.pdmodel.font.PDType1Font(
                org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA
            ),
            12,
            java.awt.Color.BLACK,
            CreationService.TextAlignment.LEFT
        );
        
        // Test withFont
        TextStyle modifiedFont = originalStyle.withFont(
            new org.apache.pdfbox.pdmodel.font.PDType1Font(
                org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD
            )
        );
        assertNotSame(originalStyle, modifiedFont);
        
        // Test withFontSize
        TextStyle modifiedSize = originalStyle.withFontSize(18);
        assertEquals(18, modifiedSize.getFontSize());
        assertEquals(12, originalStyle.getFontSize()); // Original unchanged
        
        // Test withColor
        TextStyle modifiedColor = originalStyle.withColor(java.awt.Color.RED);
        assertEquals(java.awt.Color.RED, modifiedColor.getColor());
        assertEquals(java.awt.Color.BLACK, originalStyle.getColor()); // Original unchanged
        
        // Test withAlignment
        TextStyle modifiedAlignment = originalStyle.withAlignment(CreationService.TextAlignment.CENTER);
        assertEquals(CreationService.TextAlignment.CENTER, modifiedAlignment.getAlignment());
        assertEquals(CreationService.TextAlignment.LEFT, originalStyle.getAlignment()); // Original unchanged
    }
}
