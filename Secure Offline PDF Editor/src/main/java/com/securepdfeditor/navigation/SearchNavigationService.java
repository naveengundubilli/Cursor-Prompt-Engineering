package com.securepdfeditor.navigation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchNavigationService {
    private static final Logger logger = LoggerFactory.getLogger(SearchNavigationService.class);

    private PDDocument document;

    public void setDocument(PDDocument document) { this.document = document; }
    public boolean isDocumentLoaded() { return document != null; }

    public static class Match {
        public final int pageIndex;
        public final int startIndex;
        public final int endIndex;
        public final String excerpt;
        public Match(int pageIndex, int startIndex, int endIndex, String excerpt) {
            this.pageIndex = pageIndex;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.excerpt = excerpt;
        }
        @Override public String toString() { return "Match{"+pageIndex+","+startIndex+","+endIndex+"}"; }
    }

    // Simple full-text search across pages
    public List<Match> search(String query) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (query == null) {
            throw new IllegalArgumentException("Search query cannot be null");
        }
        
        List<Match> matches = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper();
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            stripper.setStartPage(i + 1);
            stripper.setEndPage(i + 1);
            String text = stripper.getText(document);
            int idx = 0;
            String lower = text.toLowerCase();
            String q = query.toLowerCase();
            while ((idx = lower.indexOf(q, idx)) >= 0) {
                int end = idx + q.length();
                int from = Math.max(0, idx - 20);
                int to = Math.min(text.length(), end + 20);
                String excerpt = text.substring(from, to).replaceAll("\r?\n", " ");
                matches.add(new Match(i, idx, end, excerpt));
                idx = end;
            }
        }
        logger.info("Search '{}' found {} matches", query, matches.size());
        return matches;
    }

    // Add a simple bookmark to a page
    public void addBookmark(String title, int pageIndex) {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Bookmark title cannot be null or empty");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("Bookmark title too long (max 255 characters)");
        }
        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
            throw new IllegalArgumentException("Invalid page index: " + pageIndex);
        }
        
        // Validate for special characters that might cause issues in PDF bookmarks
        if (title.matches(".*[@#$%^&*()\\[\\]{}|\\\\:;\"'<>?/].*")) {
            throw new IllegalArgumentException("Bookmark title contains invalid special characters");
        }
        
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDDocumentOutline outline = catalog.getDocumentOutline();
        if (outline == null) {
            outline = new PDDocumentOutline();
            catalog.setDocumentOutline(outline);
        }
        PDOutlineItem item = new PDOutlineItem();
        item.setTitle(title);
        item.setDestination(document.getPage(pageIndex));
        outline.addLast(item);
        outline.openNode();
        item.openNode();
        logger.info("Added bookmark '{}' to page {}", title, pageIndex);
    }

    // Generate a simple table of contents page appended at the end
    public void generateSimpleTOC(List<String> entries) throws IOException {
        if (!isDocumentLoaded()) throw new IllegalStateException("No document loaded");
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("TOC entries cannot be null or empty");
        }
        
        PDPage tocPage = new PDPage(PDRectangle.LETTER);
        document.addPage(tocPage);
        try (PDPageContentStream cs = new PDPageContentStream(document, tocPage)) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            cs.beginText();
            cs.setFont(font, 18);
            cs.newLineAtOffset(72, 720);
            cs.showText("Table of Contents");
            cs.endText();

            PDType1Font font2 = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float y = 690;
            for (String e : entries) {
                cs.beginText();
                cs.setFont(font2, 12);
                cs.newLineAtOffset(72, y);
                cs.showText(e);
                cs.endText();
                y -= 18;
                if (y < 72) break;
            }
        }
        logger.info("Appended simple TOC page with {} entries", entries.size());
    }
}


