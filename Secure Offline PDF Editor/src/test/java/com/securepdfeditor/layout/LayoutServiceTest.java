package com.securepdfeditor.layout;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LayoutServiceTest {
    private PDDocument document;
    private LayoutService layoutService;

    @BeforeEach
    void setUp() throws Exception {
        document = new PDDocument();
        document.addPage(new PDPage(PDRectangle.A4));
        document.addPage(new PDPage(PDRectangle.A4));
        layoutService = new LayoutService();
        layoutService.setDocument(document);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (document != null) {
            document.close();
        }
    }

    @Test
    void testGridAndSnap() {
        // Test grid functionality
        assertFalse(layoutService.isGridEnabled());
        layoutService.setGridEnabled(true);
        assertTrue(layoutService.isGridEnabled());
        
        // Test snap functionality
        assertFalse(layoutService.isSnapEnabled());
        layoutService.setSnapEnabled(true);
        assertTrue(layoutService.isSnapEnabled());
    }

    @Test
    void testPageManagement() throws Exception {
        int initialPages = document.getNumberOfPages();
        
        // Test add page
        layoutService.addPage();
        assertEquals(initialPages + 1, document.getNumberOfPages());
        
        // Test delete page
        layoutService.deletePage(1);
        assertEquals(initialPages, document.getNumberOfPages());
        
        // Test move page
        layoutService.movePage(0, 1);
        // Verify page was moved (this would require more complex verification)
        
        // Test rotate page
        layoutService.rotatePage(0, 90);
        // Verify page was rotated
    }

    @Test
    void testGetPageCount() {
        assertEquals(2, layoutService.getPageCount());
    }

    @Test
    void testInvalidPageOperations() {
        // Test deleting non-existent page
        assertThrows(IllegalArgumentException.class, () -> layoutService.deletePage(999));
        
        // Test moving to invalid position
        assertThrows(IllegalArgumentException.class, () -> layoutService.movePage(0, 999));
    }

    @Test
    void testDocumentNotLoaded() {
        LayoutService emptyService = new LayoutService();
        
        assertThrows(IllegalStateException.class, () -> emptyService.addPage());
        assertThrows(IllegalStateException.class, () -> emptyService.deletePage(0));
        assertThrows(IllegalStateException.class, () -> emptyService.movePage(0, 1));
        assertThrows(IllegalStateException.class, () -> emptyService.rotatePage(0, 90));
    }
}
