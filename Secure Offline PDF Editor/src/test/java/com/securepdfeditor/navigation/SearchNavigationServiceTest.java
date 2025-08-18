package com.securepdfeditor.navigation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchNavigationServiceTest {
    private PDDocument doc;
    private SearchNavigationService nav;

    @BeforeEach
    void setUp() throws Exception {
        doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(100, 700);
            cs.showText("Hello Milestone 3");
            cs.endText();
        }
        nav = new SearchNavigationService();
        nav.setDocument(doc);
    }

    @AfterEach
    void tearDown() throws Exception {
        doc.close();
    }

    @Test
    void searchFindsText() throws Exception {
        List<SearchNavigationService.Match> res = nav.search("Milestone");
        assertFalse(res.isEmpty());
    }
}


