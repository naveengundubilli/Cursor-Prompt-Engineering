package com.securepdfeditor.creation;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;

/**
 * Grid paper template with a grid pattern for drawing and diagrams.
 */
public class GridPageTemplate implements Template {
    
    @Override
    public PDPage createPage() throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(new org.apache.pdfbox.pdmodel.PDDocument(), page, 
                                                                       PDPageContentStream.AppendMode.APPEND, true, true)) {
            
            PDRectangle pageRect = page.getMediaBox();
            float pageWidth = pageRect.getWidth();
            float pageHeight = pageRect.getHeight();
            
            // Draw grid lines
            contentStream.setStrokingColor(Color.LIGHT_GRAY);
            contentStream.setLineWidth(0.25f);
            
            float gridSize = 20f; // 20 points grid
            float topMargin = 50f;
            float bottomMargin = 50f;
            float leftMargin = 50f;
            float rightMargin = 50f;
            
            // Draw vertical lines
            for (float x = leftMargin; x <= pageWidth - rightMargin; x += gridSize) {
                contentStream.moveTo(x, topMargin);
                contentStream.lineTo(x, pageHeight - bottomMargin);
                contentStream.stroke();
            }
            
            // Draw horizontal lines
            for (float y = topMargin; y <= pageHeight - bottomMargin; y += gridSize) {
                contentStream.moveTo(leftMargin, y);
                contentStream.lineTo(pageWidth - rightMargin, y);
                contentStream.stroke();
            }
            
            // Draw thicker lines every 5 grid cells (100 points)
            contentStream.setLineWidth(0.5f);
            contentStream.setStrokingColor(Color.GRAY);
            
            for (float x = leftMargin; x <= pageWidth - rightMargin; x += gridSize * 5) {
                contentStream.moveTo(x, topMargin);
                contentStream.lineTo(x, pageHeight - bottomMargin);
                contentStream.stroke();
            }
            
            for (float y = topMargin; y <= pageHeight - bottomMargin; y += gridSize * 5) {
                contentStream.moveTo(leftMargin, y);
                contentStream.lineTo(pageWidth - rightMargin, y);
                contentStream.stroke();
            }
            
            // Add page title
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.beginText();
            contentStream.newLineAtOffset(leftMargin, pageHeight - 30);
            contentStream.showText("Grid Paper");
            contentStream.endText();
        }
        
        return page;
    }
    
    @Override
    public String getName() {
        return "Grid Page";
    }
    
    @Override
    public String getDescription() {
        return "A grid paper template with fine grid lines for drawing and diagrams";
    }
}

