package com.securepdfeditor.creation;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;

/**
 * Simple blank page template with no pre-defined content.
 */
public class BlankTemplate implements Template {
    
    @Override
    public PDPage createPage() throws IOException {
        return new PDPage(PDRectangle.A4);
    }
    
    @Override
    public String getName() {
        return "Blank";
    }
    
    @Override
    public String getDescription() {
        return "A clean blank page with no pre-defined content";
    }
}

