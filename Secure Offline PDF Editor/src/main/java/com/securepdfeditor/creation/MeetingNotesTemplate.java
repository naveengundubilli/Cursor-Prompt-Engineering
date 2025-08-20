package com.securepdfeditor.creation;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;

/**
 * Meeting notes template with sections for agenda, attendees, and action items.
 */
public class MeetingNotesTemplate implements Template {
    
    @Override
    public PDPage createPage() throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        
        try (PDPageContentStream contentStream = new PDPageContentStream(new org.apache.pdfbox.pdmodel.PDDocument(), page, 
                                                                       PDPageContentStream.AppendMode.APPEND, true, true)) {
            
            PDRectangle pageRect = page.getMediaBox();
            float pageWidth = pageRect.getWidth();
            float pageHeight = pageRect.getHeight();
            
            // Header
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, pageHeight - 50);
            contentStream.showText("Meeting Notes");
            contentStream.endText();
            
            // Meeting details section
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            contentStream.setNonStrokingColor(Color.DARK_GRAY);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, pageHeight - 100);
            contentStream.showText("Meeting Details:");
            contentStream.endText();
            
            // Meeting details fields
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.setNonStrokingColor(Color.BLACK);
            
            String[] details = {"Date: _________________", "Time: _________________", "Location: _________________"};
            float y = pageHeight - 130;
            for (String detail : details) {
                contentStream.beginText();
                contentStream.newLineAtOffset(70, y);
                contentStream.showText(detail);
                contentStream.endText();
                y -= 20;
            }
            
            // Attendees section
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            contentStream.setNonStrokingColor(Color.DARK_GRAY);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, y - 20);
            contentStream.showText("Attendees:");
            contentStream.endText();
            
            // Attendees list
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.setNonStrokingColor(Color.BLACK);
            y -= 50;
            for (int i = 1; i <= 8; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(70, y);
                contentStream.showText(i + ". _________________");
                contentStream.endText();
                y -= 20;
            }
            
            // Agenda section
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            contentStream.setNonStrokingColor(Color.DARK_GRAY);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, y - 20);
            contentStream.showText("Agenda:");
            contentStream.endText();
            
            // Agenda items
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.setNonStrokingColor(Color.BLACK);
            y -= 50;
            for (int i = 1; i <= 5; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(70, y);
                contentStream.showText(i + ". _________________");
                contentStream.endText();
                y -= 20;
            }
            
            // Action Items section
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            contentStream.setNonStrokingColor(Color.DARK_GRAY);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, y - 20);
            contentStream.showText("Action Items:");
            contentStream.endText();
            
            // Action items table
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.setNonStrokingColor(Color.BLACK);
            y -= 50;
            
            // Table headers
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(70, y);
            contentStream.showText("Action Item");
            contentStream.endText();
            contentStream.beginText();
            contentStream.newLineAtOffset(300, y);
            contentStream.showText("Owner");
            contentStream.endText();
            contentStream.beginText();
            contentStream.newLineAtOffset(400, y);
            contentStream.showText("Due Date");
            contentStream.endText();
            
            // Table rows
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            y -= 20;
            for (int i = 1; i <= 4; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(70, y);
                contentStream.showText(i + ". _________________");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(300, y);
                contentStream.showText("_________________");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(400, y);
                contentStream.showText("_________________");
                contentStream.endText();
                y -= 20;
            }
            
            // Notes section
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            contentStream.setNonStrokingColor(Color.DARK_GRAY);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, y - 20);
            contentStream.showText("Notes:");
            contentStream.endText();
            
            // Notes lines
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.setNonStrokingColor(Color.BLACK);
            y -= 50;
            for (int i = 0; i < 6; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(70, y);
                contentStream.showText("_________________________________________________________________");
                contentStream.endText();
                y -= 25;
            }
        }
        
        return page;
    }
    
    @Override
    public String getName() {
        return "Meeting Notes";
    }
    
    @Override
    public String getDescription() {
        return "A meeting notes template with sections for agenda, attendees, action items, and notes";
    }
}

