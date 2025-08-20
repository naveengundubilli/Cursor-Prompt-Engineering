package com.securepdfeditor.creation;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;

/**
 * Lined paper template with horizontal lines for writing.
 */
public class LinedPageTemplate implements Template {
    
    @Override
    public PDPage createPage() throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(new org.apache.pdfbox.pdmodel.PDDocument(), page, 
                                                                       PDPageContentStream.AppendMode.APPEND, true, true)) {
            
            PDRectangle pageRect = page.getMediaBox();
            float pageWidth = pageRect.getWidth();
            float pageHeight = pageRect.getHeight();
            
            // Draw horizontal lines
            contentStream.setStrokingColor(Color.LIGHT_GRAY);
            contentStream.setLineWidth(0.5f);
            
            float lineSpacing = 20f; // 20 points between lines
            float topMargin = 50f;
            float bottomMargin = 50f;
            float leftMargin = 50f;
            float rightMargin = 50f;
            
            // Draw lines from top to bottom
            for (float y = topMargin; y <= pageHeight - bottomMargin; y += lineSpacing) {
                contentStream.moveTo(leftMargin, y);
                contentStream.lineTo(pageWidth - rightMargin, y);
                contentStream.stroke();
            }
            
            // Draw left margin line
            contentStream.setLineWidth(1.5f);
            contentStream.setStrokingColor(Color.RED);
            contentStream.moveTo(leftMargin, topMargin);
            contentStream.lineTo(leftMargin, pageHeight - bottomMargin);
            contentStream.stroke();
            
            // Add page title
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.beginText();
            contentStream.newLineAtOffset(leftMargin, pageHeight - 30);
            contentStream.showText("Lined Paper");
            contentStream.endText();
        }
        
        return page;
    }
    
    @Override
    public String getName() {
        return "Lined Page";
    }
    
    @Override
    public String getDescription() {
        return "A lined paper template with horizontal lines and red left margin";
    }
}
