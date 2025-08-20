package com.securepdfeditor.creation;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;

/**
 * Title page template with centered title and subtitle areas.
 */
public class TitlePageTemplate implements Template {
    
    @Override
    public PDPage createPage() throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(new org.apache.pdfbox.pdmodel.PDDocument(), page, 
                                                                       PDPageContentStream.AppendMode.APPEND, true, true)) {
            
            PDRectangle pageRect = page.getMediaBox();
            float pageWidth = pageRect.getWidth();
            float pageHeight = pageRect.getHeight();
            
            // Draw decorative border
            contentStream.setStrokingColor(Color.DARK_GRAY);
            contentStream.setLineWidth(2);
            contentStream.addRect(50, 50, pageWidth - 100, pageHeight - 100);
            contentStream.stroke();
            
            // Add inner border
            contentStream.setStrokingColor(Color.LIGHT_GRAY);
            contentStream.setLineWidth(1);
            contentStream.addRect(70, 70, pageWidth - 140, pageHeight - 140);
            contentStream.stroke();
            
            // Add title placeholder
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 36);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.beginText();
            contentStream.newLineAtOffset(pageWidth / 2 - 100, pageHeight / 2 + 50);
            contentStream.showText("TITLE");
            contentStream.endText();
            
            // Add subtitle placeholder
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 18);
            contentStream.setNonStrokingColor(Color.GRAY);
            contentStream.beginText();
            contentStream.newLineAtOffset(pageWidth / 2 - 80, pageHeight / 2);
            contentStream.showText("Subtitle");
            contentStream.endText();
            
            // Add author placeholder
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
            contentStream.setNonStrokingColor(Color.DARK_GRAY);
            contentStream.beginText();
            contentStream.newLineAtOffset(pageWidth / 2 - 60, pageHeight / 2 - 50);
            contentStream.showText("Author Name");
            contentStream.endText();
            
            // Add date placeholder
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.setNonStrokingColor(Color.GRAY);
            contentStream.beginText();
            contentStream.newLineAtOffset(pageWidth / 2 - 40, pageHeight / 2 - 100);
            contentStream.showText("Date");
            contentStream.endText();
            
            // Add decorative line
            contentStream.setStrokingColor(Color.DARK_GRAY);
            contentStream.setLineWidth(1);
            contentStream.moveTo(pageWidth / 2 - 100, pageHeight / 2 - 120);
            contentStream.lineTo(pageWidth / 2 + 100, pageHeight / 2 - 120);
            contentStream.stroke();
        }
        
        return page;
    }
    
    @Override
    public String getName() {
        return "Title Page";
    }
    
    @Override
    public String getDescription() {
        return "A professional title page template with centered title, subtitle, and author areas";
    }
}

