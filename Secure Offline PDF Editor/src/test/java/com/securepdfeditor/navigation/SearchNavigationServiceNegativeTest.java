package com.securepdfeditor.navigation;

import com.securepdfeditor.pdf.PdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchNavigationServiceNegativeTest {
    
    private SearchNavigationService searchService;
    private PdfService pdfService;
    private PDDocument testDocument;
    
    @BeforeEach
    void setUp() throws IOException {
        searchService = new SearchNavigationService();
        pdfService = new PdfService();
        
        // Create a minimal test document
        testDocument = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        testDocument.addPage(page);
        testDocument.save("test_document.pdf");
        testDocument.close();
        
        pdfService.open(Path.of("test_document.pdf"), null);
        searchService.setDocument(pdfService.getDocument());
    }
    
    @Test
    void testSearchEmptyDocument() throws IOException {
        // Test search in empty document
        List<SearchNavigationService.Match> results = searchService.search("test");
        assertTrue(results.isEmpty(), "Search in empty document should return no results");
    }
    
    @Test
    void testSearchNullQuery() {
        // Test search with null query
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.search(null);
        }, "Search with null query should throw exception");
    }
    
    @Test
    void testSearchEmptyQuery() throws IOException {
        // Test search with empty query
        List<SearchNavigationService.Match> results = searchService.search("");
        assertTrue(results.isEmpty(), "Search with empty query should return no results");
    }
    
    @Test
    void testSearchWhitespaceQuery() throws IOException {
        // Test search with whitespace-only query
        List<SearchNavigationService.Match> results = searchService.search("   ");
        assertTrue(results.isEmpty(), "Search with whitespace query should return no results");
    }
    
    @Test
    void testSearchWithoutDocument() {
        // Test search without document loaded
        SearchNavigationService emptyService = new SearchNavigationService();
        assertThrows(IllegalStateException.class, () -> {
            emptyService.search("test");
        }, "Search without document should throw exception");
    }
    
    @Test
    void testBookmarkInvalidPage() {
        // Test bookmark creation with invalid page
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.addBookmark("Test Bookmark", -1);
        }, "Bookmark with negative page should throw exception");
        
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.addBookmark("Test Bookmark", 999);
        }, "Bookmark with out-of-bounds page should throw exception");
    }
    
    @Test
    void testBookmarkNullTitle() {
        // Test bookmark creation with null title
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.addBookmark(null, 0);
        }, "Bookmark with null title should throw exception");
    }
    
    @Test
    void testBookmarkEmptyTitle() {
        // Test bookmark creation with empty title
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.addBookmark("", 0);
        }, "Bookmark with empty title should throw exception");
    }
    
    @Test
    void testBookmarkWhitespaceTitle() {
        // Test bookmark creation with whitespace-only title
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.addBookmark("   ", 0);
        }, "Bookmark with whitespace title should throw exception");
    }
    
    @Test
    void testBookmarkWithoutDocument() {
        // Test bookmark creation without document loaded
        SearchNavigationService emptyService = new SearchNavigationService();
        assertThrows(IllegalStateException.class, () -> {
            emptyService.addBookmark("Test Bookmark", 0);
        }, "Bookmark without document should throw exception");
    }
    
    @Test
    void testGenerateTOCWithoutDocument() {
        // Test TOC generation without document loaded
        SearchNavigationService emptyService = new SearchNavigationService();
        assertThrows(IllegalStateException.class, () -> {
            emptyService.generateSimpleTOC(List.of("Test Entry"));
        }, "TOC generation without document should throw exception");
    }
    
    @Test
    void testGenerateTOCWithNullEntries() throws IOException {
        // Test TOC generation with null entries
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.generateSimpleTOC(null);
        }, "TOC generation with null entries should throw exception");
    }
    
    @Test
    void testGenerateTOCWithEmptyEntries() throws IOException {
        // Test TOC generation with empty entries
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.generateSimpleTOC(List.of());
        }, "TOC generation with empty entries should throw exception");
    }
    
    @Test
    void testSearchWithSpecialCharacters() throws IOException {
        // Test search with special characters
        List<SearchNavigationService.Match> results = searchService.search("test@#$%^&*()");
        assertTrue(results.isEmpty(), "Search with special characters should return no results in empty document");
    }
    
    @Test
    void testBookmarkTitleWithSpecialCharacters() {
        // Test bookmark creation with special characters in title
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.addBookmark("Test@#$%^&*()", 0);
        }, "Bookmark with special characters should throw exception");
    }
    
    @Test
    void testBookmarkTitleTooLong() {
        // Test bookmark creation with very long title
        String longTitle = "A".repeat(1000);
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.addBookmark(longTitle, 0);
        }, "Bookmark with very long title should throw exception");
    }
}
