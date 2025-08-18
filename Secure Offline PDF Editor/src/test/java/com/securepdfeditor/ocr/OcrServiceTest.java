package com.securepdfeditor.ocr;

import com.securepdfeditor.pdf.PdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OcrServiceTest {
    
    private OcrService ocrService;
    private PdfService pdfService;
    
    @BeforeEach
    void setUp() {
        pdfService = new PdfService();
        ocrService = new OcrService(pdfService);
    }
    
    @Test
    void testOcrServiceInitialization() {
        // Test that OCR service can be initialized
        assertNotNull(ocrService);
        // Note: OCR service may not be fully initialized without tessdata
        // This test just verifies the service can be created
    }
    
    @Test
    void testOcrServiceWithoutDocument() {
        // Test that OCR fails gracefully when no document is loaded
        assertThrows(IllegalStateException.class, () -> {
            ocrService.performOcr(null);
        });
    }
    
    @Test
    void testOcrServiceWithDocument() throws IOException {
        // Create a test PDF document
        PDDocument document = new PDDocument();
        document.addPage(new org.apache.pdfbox.pdmodel.PDPage());
        Path testPdfPath = Path.of("test_ocr_document.pdf");
        document.save(testPdfPath.toFile());
        document.close();
        
        try {
            // Open the PDF
            pdfService.open(testPdfPath, null);
            
            // Test OCR on a single page
            OcrService.OcrResult result = ocrService.performOcrOnPage(0);
            assertNotNull(result);
            assertEquals(0, result.getPageIndex());
            
        } finally {
            // Clean up
            java.nio.file.Files.deleteIfExists(testPdfPath);
        }
    }
    
    @Test
    void testOcrProgressCallback() throws IOException {
        // Create a test PDF document
        PDDocument document = new PDDocument();
        document.addPage(new org.apache.pdfbox.pdmodel.PDPage());
        Path testPdfPath = Path.of("test_ocr_progress.pdf");
        document.save(testPdfPath.toFile());
        document.close();
        
        try {
            // Open the PDF
            pdfService.open(testPdfPath, null);
            
            // Test progress callback
            boolean[] callbackCalled = {false};
            List<OcrService.OcrResult> results = ocrService.performOcr(progress -> {
                callbackCalled[0] = true;
                assertNotNull(progress);
                assertTrue(progress.getCurrentPage() > 0);
                assertTrue(progress.getTotalPages() > 0);
                assertNotNull(progress.getMessage());
            });
            
            assertTrue(callbackCalled[0]);
            assertNotNull(results);
            assertEquals(1, results.size());
            
        } finally {
            // Clean up
            java.nio.file.Files.deleteIfExists(testPdfPath);
        }
    }
}
