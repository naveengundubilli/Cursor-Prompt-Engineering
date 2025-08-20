package com.securepdfeditor.creation;

import org.apache.pdfbox.pdmodel.PDPage;

import java.io.IOException;

/**
 * Interface for PDF page templates that define layout and content.
 */
public interface Template {
    
    /**
     * Create a new page with the template's layout and content
     */
    PDPage createPage() throws IOException;
    
    /**
     * Get the template name
     */
    String getName();
    
    /**
     * Get the template description
     */
    String getDescription();
}

