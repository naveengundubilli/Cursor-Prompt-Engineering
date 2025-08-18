package com.securepdfeditor.editing;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EditingServiceTest {
    @TempDir Path tempDir;
    private EditingService editingService;
    private PDDocument document;
    private Path testPdfPath;

    @BeforeEach
    void setUp() throws IOException {
        editingService = new EditingService();

        // Create a test PDF
        document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Add some content to the page
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Test content for editing");
            contentStream.endText();
        }

        testPdfPath = tempDir.resolve("test.pdf");
        document.save(testPdfPath.toFile());
        document.close();

        // Reopen for editing
        document = Loader.loadPDF(testPdfPath.toFile());
        editingService.setDocument(document);
    }

    @Test
    void testAddText() throws IOException {
        String testText = "Added text";
        editingService.addText(0, testText, 200, 200, 14, Color.BLACK);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.TEXT_ADD, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(testText, action.getContent());
        assertEquals(200, action.getX());
        assertEquals(200, action.getY());
        assertEquals(14, action.getFontSize());
        assertEquals(Color.BLACK, action.getTextColor());
    }

    @Test
    void testDeleteText() throws IOException {
        editingService.deleteText(0, 100, 100, 50, 20);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.TEXT_DELETE, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(100, action.getX());
        assertEquals(100, action.getY());
        assertEquals(50, action.getWidth());
        assertEquals(20, action.getHeight());
    }

    @Test
    void testInsertImage() throws IOException {
        // Create a test image
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Path imagePath = tempDir.resolve("test.png");
        javax.imageio.ImageIO.write(testImage, "PNG", imagePath.toFile());

        editingService.insertImage(0, imagePath, 150, 150, 100, 100);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.IMAGE_INSERT, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(imagePath.toString(), action.getContent());
        assertEquals(150, action.getX());
        assertEquals(150, action.getY());
        assertEquals(100, action.getWidth());
        assertEquals(100, action.getHeight());
    }

    @Test
    void testDrawRectangle() throws IOException {
        Color fillColor = Color.YELLOW;
        Color strokeColor = Color.BLACK;
        float strokeWidth = 2.0f;

        editingService.drawRectangle(0, 100, 100, 200, 100, fillColor, strokeColor, strokeWidth);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.SHAPE_RECTANGLE, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(100, action.getX());
        assertEquals(100, action.getY());
        assertEquals(200, action.getWidth());
        assertEquals(100, action.getHeight());
        assertEquals(fillColor, action.getFillColor());
        assertEquals(strokeColor, action.getStrokeColor());
        assertEquals(strokeWidth, action.getStrokeWidth());
    }

    @Test
    void testDrawCircle() throws IOException {
        Color fillColor = Color.CYAN;
        Color strokeColor = Color.BLUE;
        float strokeWidth = 1.5f;

        editingService.drawCircle(0, 200, 200, 50, fillColor, strokeColor, strokeWidth);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.SHAPE_CIRCLE, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(200, action.getX());
        assertEquals(200, action.getY());
        assertEquals(50, action.getWidth()); // radius stored as width
        assertEquals(50, action.getHeight()); // radius stored as height
        assertEquals(fillColor, action.getFillColor());
        assertEquals(strokeColor, action.getStrokeColor());
        assertEquals(strokeWidth, action.getStrokeWidth());
    }

    @Test
    void testDrawLine() throws IOException {
        Color lineColor = Color.RED;
        float strokeWidth = 3.0f;

        editingService.drawLine(0, 50, 50, 300, 300, lineColor, strokeWidth);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.SHAPE_LINE, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(50, action.getX());
        assertEquals(50, action.getY());
        assertEquals(250, action.getWidth()); // x2 - x1
        assertEquals(250, action.getHeight()); // y2 - y1
        assertEquals(lineColor, action.getStrokeColor());
        assertEquals(strokeWidth, action.getStrokeWidth());
    }

    @Test
    void testAddHighlight() throws IOException {
        Color highlightColor = Color.YELLOW;

        editingService.addHighlight(0, 100, 100, 200, 20, highlightColor);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.ANNOTATION_HIGHLIGHT, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(100, action.getX());
        assertEquals(100, action.getY());
        assertEquals(200, action.getWidth());
        assertEquals(20, action.getHeight());
        assertEquals(highlightColor, action.getFillColor());
    }

    @Test
    void testAddUnderline() throws IOException {
        Color underlineColor = Color.BLUE;

        editingService.addUnderline(0, 100, 120, 200, 5, underlineColor);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.ANNOTATION_UNDERLINE, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(100, action.getX());
        assertEquals(120, action.getY());
        assertEquals(200, action.getWidth());
        assertEquals(5, action.getHeight());
        assertEquals(underlineColor, action.getFillColor());
    }

    @Test
    void testAddStrikethrough() throws IOException {
        Color strikethroughColor = Color.RED;

        editingService.addStrikethrough(0, 100, 110, 200, 5, strikethroughColor);

        List<EditAction> history = editingService.getEditHistory();
        assertEquals(1, history.size());

        EditAction action = history.get(0);
        assertEquals(EditingService.EditType.ANNOTATION_STRIKETHROUGH, action.getType());
        assertEquals(0, action.getPageIndex());
        assertEquals(100, action.getX());
        assertEquals(110, action.getY());
        assertEquals(200, action.getWidth());
        assertEquals(5, action.getHeight());
        assertEquals(strikethroughColor, action.getFillColor());
    }

    @Test
    void testEditHistory() {
        assertEquals(0, editingService.getHistorySize());

        try {
            editingService.addText(0, "Test", 100, 100, 12, Color.BLACK);
            assertEquals(1, editingService.getHistorySize());

            editingService.drawRectangle(0, 100, 100, 50, 50, Color.RED, Color.BLACK, 1);
            assertEquals(2, editingService.getHistorySize());

            List<EditAction> history = editingService.getEditHistory();
            assertEquals(2, history.size());

            editingService.clearHistory();
            assertEquals(0, editingService.getHistorySize());
            assertTrue(editingService.getEditHistory().isEmpty());
        } catch (IOException e) {
            fail("Test failed with IOException: " + e.getMessage());
        }
    }

    @Test
    void testInvalidPageIndex() {
        assertThrows(IllegalArgumentException.class, () -> {
            editingService.addText(-1, "Test", 100, 100, 12, Color.BLACK);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            editingService.addText(10, "Test", 100, 100, 12, Color.BLACK);
        });
    }

    @Test
    void testNoDocumentLoaded() {
        editingService.setDocument(null);

        assertThrows(IllegalStateException.class, () -> {
            editingService.addText(0, "Test", 100, 100, 12, Color.BLACK);
        });

        assertThrows(IllegalStateException.class, () -> {
            editingService.drawRectangle(0, 100, 100, 50, 50, Color.RED, Color.BLACK, 1);
        });
    }
}
